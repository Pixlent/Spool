package me.pixlent;

import com.caoccao.javet.enums.JSRuntimeType;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.converters.JavetProxyConverter;
import com.caoccao.javet.interop.engine.IJavetEngine;
import com.caoccao.javet.interop.engine.JavetEnginePool;
import com.caoccao.javet.node.modules.NodeModuleModule;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueObject;
import com.google.gson.Gson;
import me.pixlent.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum Spool {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(Spool.class);

    public static final int[] MIN_ENGINE_VERSION = {1, 0, 0};
    public static final int FORMAT = 1;

    // Javet
    public final JavetEnginePool<NodeRuntime> enginePool;
    public final IJavetEngine<NodeRuntime> engine;
    public final NodeRuntime nodeRuntime;
    public final JavetProxyConverter proxyConverter;
    // Modules
    private final Gson gson = new Gson();
    private final List<File> modules = new ArrayList<>();
    private final List<File> entryPoints = new ArrayList<>();

    Spool() {
        try {
            // Force V8 initialization before pool creation
            try (V8Runtime v8Runtime = V8Host.getNodeInstance().createV8Runtime()) {
                v8Runtime.getExecutor("console.log('Initializing V8')").executeVoid();
            }

            enginePool = new JavetEnginePool<>();
            enginePool.getConfig().setJSRuntimeType(JSRuntimeType.Node);
            engine = enginePool.getEngine();
            nodeRuntime = engine.getV8Runtime();

            V8Host.getNodeInstance().enableGCNotification();
            proxyConverter = new JavetProxyConverter();
            nodeRuntime.setConverter(proxyConverter);
            nodeRuntime.setLogger(new JavetSpoolLogger());
            nodeRuntime.setV8ModuleResolver(new VirtualModuleResolver());

            nodeRuntime.getNodeModule(NodeModuleModule.class)
                    .setRequireRootDirectory(FileUtils.getBasePath().resolve("./node_modules/").toFile());
        } catch (JavetException e) {
            throw new RuntimeException("Failed to initialize NodeInstance", e);
        }
    }

    public void start() {
        for (File file : Objects.requireNonNull(FileUtils.getBasePath().resolve("./node/modules/").toFile().listFiles())) {
            File manifest = file.toPath().resolve("./manifest.json").toFile();
            Spool.INSTANCE.register(manifest);
        }

        Spool.INSTANCE.index();
        for (File entryPoint : Spool.INSTANCE.getEntryPoints()) {
            execute(entryPoint);
        }
    }

    // Cleanup hook for graceful shutdown
    public void shutdown() {
        try {
            if (engine != null) engine.close();
            if (enginePool != null) enginePool.close();
        } catch (Exception e) {
            throw new RuntimeException("Shutdown failed", e);
        }
    }

    public void execute(File file) {
        try {
            nodeRuntime.getExecutor(file)
                    .setModule(true)
                    .setResourceName(file.getAbsolutePath())
                    .executeVoid();
        } catch (JavetException e) {
            throw new RuntimeException("Execution failed for: " + file.getName(), e);
        }
    }

    public void register(File manifest) {
        modules.add(manifest);
    }

    public void index() {
        entryPoints.clear();
        modules.forEach((file) -> {
            ModuleManifest manifest = gson.fromJson(FileUtils.readFile(file), ModuleManifest.class);
            Path modulePath = file.toPath().getParent().toAbsolutePath().normalize();

            if (manifest == null) {
                LOGGER.error("Could not read manifest from {}", file);
                return;
            }
            if (manifest.format() != FORMAT) {
                LOGGER.error("Illegal format version: {} in {}", manifest.format(), file.getPath());
                return;
            }
            if (manifest.header().minEngineVersion()[0] != Spool.MIN_ENGINE_VERSION[0]) {
                LOGGER.error("{} engine version is incompatible with current engine min_version: {} in module: {}",
                        manifest.header().minEngineVersion(), Spool.MIN_ENGINE_VERSION, file.getPath());
                return;
            }
            if (manifest.header().minEngineVersion()[1] > Spool.MIN_ENGINE_VERSION[1]) {
                LOGGER.error("Minimum engine version required for module: {} is not met", file.getPath());
                return;
            }
            if (manifest.header().minEngineVersion()[1] == Spool.MIN_ENGINE_VERSION[1]
                    && manifest.header().minEngineVersion()[2] > Spool.MIN_ENGINE_VERSION[2]) {
                LOGGER.error("Minimum engine version required for module: {} is not met", file.getPath());
                return;
            }

            for (String path : manifest.header().entryPoints()) {
                Path entryPoint = modulePath.resolve(path).toAbsolutePath().normalize();

                if (!entryPoint.startsWith(modulePath)) {
                    LOGGER.error("Entry point {} is not in its modules path", entryPoint);
                    break;
                }

                entryPoints.add(entryPoint.toFile());
            }
        });
    }

    public File[] getModules() {
        return modules.toArray(new File[0]);
    }

    public File[] getEntryPoints() {
        return entryPoints.toArray(new File[0]);
    }

    public static V8ModuleBuilder moduleBuilder() {
        return new V8ModuleBuilder();
    }

    public static final class V8ModuleBuilder {
        private final V8ValueObject moduleExports;

        private V8ModuleBuilder() {
            try {
                this.moduleExports = INSTANCE.nodeRuntime.createV8ValueObject();
            } catch (JavetException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Exports an object to the module.
         *
         * @param name  The name of the object in the module.
         * @param value The object to export.
         * @return The current builder instance for chaining.
         */
        public V8ModuleBuilder exportObject(String name, Object value) {
            try {
                moduleExports.set(name, value);
                return this;
            } catch (JavetException e) {
                throw new RuntimeException("Failed to export object: " + name, e);
            }
        }

        /**
         * Exports a class to the module.
         *
         * @param name  The name of the class in the module.
         * @param value The class to export.
         * @return The current builder instance for chaining.
         */
        public V8ModuleBuilder exportClass(String name, Class<?> value) {
            try {
                moduleExports.set(name, INSTANCE.proxyConverter.toV8Value(INSTANCE.nodeRuntime, value));
                return this;
            } catch (JavetException e) {
                throw new RuntimeException("Failed to export class: " + name, e);
            }
        }

        public V8ModuleBuilder exportValue(String name, V8Value value) {
            try {
                moduleExports.set(name, value);
                return this;
            } catch (JavetException e) {
                throw new RuntimeException("Failed to export value: " + name, e);
            }
        }

        /**
         * Builds and compiles the module into a V8Module object.
         */
        public void build(String name) {
            try {
                INSTANCE.nodeRuntime.createV8Module(name, moduleExports);
            } catch (JavetException e) {
                throw new RuntimeException("Failed to compile JavaModule into V8Module.", e);
            }
        }
    }
}