package me.pixlent;

import lombok.Setter;
import me.pixlent.module.JSModule;
import me.pixlent.module.RequireApi;
import me.pixlent.module.SpoolModule;
import me.pixlent.signal.AbstractSignal;
import me.pixlent.signal.SignalListener;
import me.pixlent.utils.FileUtils;
import me.pixlent.utils.ValueUtils;
import org.graalvm.polyglot.*;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public enum SpoolApi {
    INSTANCE;

    public final String language = "js";
    public final Engine engine = Engine.newBuilder(language)
            //.sandbox(SandboxPolicy.UNTRUSTED)
            .allowExperimentalOptions(true)
            .option("engine.WarnInterpreterOnly", "false")
            .option("engine.CompileImmediately", "true")  // Enable JIT for speed
            .option("engine.Compilation", "true")         // Enable ahead-of-time (AOT) compilation
            .build();
    public final Context privileged = Context.newBuilder(language)
            .engine(engine)
            .allowAllAccess(true)
            .build();
    public Context sandbox;

    // Registry
    private static final Map<String, SpoolModule> modules = new HashMap<>();
    private static final Map<Path, Value> exports = new HashMap<>();

    private final List<Path> indexers = new ArrayList<>();

    private final ReloadSignal reloadSignal = new ReloadSignal();
    private final IndexSignal indexSignal = new IndexSignal();
    private final ModuleResolverSignal moduleResolverSignal = new ModuleResolverSignal();

    SpoolApi() {

    }

    public void open() {
        sandbox = Context.newBuilder(language)
                .engine(engine)
                .allowHostAccess(HostAccess.ALL)
                .allowPolyglotAccess(PolyglotAccess.ALL)
                //.sandbox(SandboxPolicy.UNTRUSTED)
//                .allowHostClassLookup(className -> true)
//                .allowNativeAccess(true)
//                .allowHostClassLoading(true)
//                .allowInnerContextOptions(true)
                .build();

        reloadSignal.emit(new ReloadSignal.ReloadEvent(ReloadSignal.LoadState.PRE_INIT));
        indexSignal.emit(new IndexSignal.IndexEvent(IndexSignal.IndexStage.REGISTER));
        modules.forEach((name, module) -> {
            if (module instanceof JSModule jsModule) {
                jsModule.initialize();
            }
        });
        indexers.forEach(directory -> FileUtils.searchDirectoryDeep(directory).forEach(file -> {
            if (file.getName().endsWith(".js")) execute(file);
        }));
        indexSignal.emit(new IndexSignal.IndexEvent(IndexSignal.IndexStage.EXECUTE));
    }

    public void close() {
        sandbox.close();
        sandbox = null;
        clearModules();
        clearExports();
    }

    public void reload() {
        reloadSignal.emit(new ReloadSignal.ReloadEvent(ReloadSignal.LoadState.PRE_CLOSE));
        close();
        open();
        reloadSignal.emit(new ReloadSignal.ReloadEvent(ReloadSignal.LoadState.POST));
    }

    public void execute(File file) {
        execute(file, Map.of());
    }

    public void execute(File file, Map<String, Object> args) {
        if (exports.containsKey(file.toPath())) return;

        String code = "(require, exports";

        Value exports = ValueUtils.emptyObject();
        List<Object> arguments = new ArrayList<>();

        for (Map.Entry<String, Object> entry : args.entrySet()) {
            code = code.concat(", " + entry.getKey());
            arguments.add(entry.getValue());
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        code = code.concat(") => {\n" + FileUtils.readFile(file) + "\n}");

        Value function = sandbox.eval(Source
                .newBuilder("js", code, file.getName())
                .buildLiteral());

        if (function.canExecute()) {
            try {
                function.executeVoid(RequireApi.makeRequire(file.toPath().getParent()), exports, arguments.toArray());
                SpoolApi.exports.put(file.toPath(), exports);
                System.out.println(exports);
            } catch (PolyglotException e) {
                // Print detailed error information
                if (e.isSyntaxError()) {
                    System.err.println(e.getMessage());
                } else {
                    if (e.getSourceLocation() != null) {
                        System.err.println("Error in " + file.getPath() + " @" + (e.getSourceLocation().getStartLine() - 1) +
                                ":" + e.getSourceLocation().getStartColumn() + " - " + e.getMessage());
                    } else System.err.println(e.getMessage());e.getMessage();
                }
            } catch (Exception e) {
                Arrays.stream(e.getStackTrace()).forEach(System.err::println);
            }
        }
    }

    public Value execute(String source) {
        return sandbox.eval(Source.create(language, source));
    }

    public void index(Path path) {
        indexers.add(path);
    }

    public boolean containsModule(String name) {
        return modules.containsKey(name);
    }

    public boolean containsExport(Path path) {
        return exports.containsKey(path);
    }

    public @Nullable Value fetchModule(String name) {
        if (!modules.containsKey(name)) {
            ModuleResolverSignal.ModuleResolverEvent event = new ModuleResolverSignal.ModuleResolverEvent(name);

            moduleResolverSignal.emit(event);

            if (event.module == null) {
                System.out.println("Module " + name + " doesn't exist");
                return null;
            }
            return event.module;
        }

        return modules.get(name).getExports();
    }

    public @Nullable Value fetchExports(Path path) {
        if (exports.containsKey(path)) return exports.get(path);
        return null;
    }

    public void clearModules() {
        modules.clear();
    }

    public void clearExports() {
        exports.clear();
    }

    public void subscribeToReload(SignalListener<ReloadSignal.ReloadEvent> signalListener) {
        reloadSignal.subscribe(signalListener);
    }

    public void unsubscribeToReload(SignalListener<ReloadSignal.ReloadEvent> signalListener) {
        reloadSignal.unsubscribe(signalListener);
    }

    public void subscribeToIndex(SignalListener<IndexSignal.IndexEvent> signalListener) {
        indexSignal.subscribe(signalListener);
    }

    public void unsubscribeToIndex(SignalListener<IndexSignal.IndexEvent> signalListener) {
        indexSignal.unsubscribe(signalListener);
    }

    public void subscribeToModuleResolver(SignalListener<ModuleResolverSignal.ModuleResolverEvent> signalListener) {
        moduleResolverSignal.subscribe(signalListener);
    }

    public void unsubscribeToModuleResolver(SignalListener<ModuleResolverSignal.ModuleResolverEvent> signalListener) {
        moduleResolverSignal.unsubscribe(signalListener);
    }

    public static class ReloadSignal extends AbstractSignal<ReloadSignal.ReloadEvent> {
        public record ReloadEvent(LoadState state) { }
        public enum LoadState {
            PRE_CLOSE,
            PRE_INIT,
            POST
        }
    }

    public static class IndexSignal extends AbstractSignal<IndexSignal.IndexEvent> {
        public record IndexEvent(IndexStage stage) {
            public void register(JSModule module) {
                modules.put(module.getManifest().name(), module);
            }

            public void register(String name, SpoolModule module) {
                modules.put(name, module);
            }

            public void register(Path path, Value exportedValues) {
                exports.put(path, exportedValues);
            }
        }
        public enum IndexStage {
            REGISTER,
            EXECUTE
        }
    }

    public static class ModuleResolverSignal extends AbstractSignal<ModuleResolverSignal.ModuleResolverEvent> {
        public static class ModuleResolverEvent {
            public final String moduleName;
            public Value module = null;

            public ModuleResolverEvent(String moduleName) {
                this.moduleName = moduleName;
            }
        }
    }
}
