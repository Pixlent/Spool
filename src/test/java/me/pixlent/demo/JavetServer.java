package me.pixlent.demo;

import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.converters.JavetProxyConverter;
import com.caoccao.javet.javenode.JNEventLoop;
import com.caoccao.javet.javenode.enums.JNModuleType;
import com.caoccao.javet.values.reference.V8Module;
import com.caoccao.javet.values.reference.V8ValueObject;
import me.pixlent.demo.api.SystemApi;
import me.pixlent.demo.javet.CustomJavetLogger;
import me.pixlent.utils.FileUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavetServer {
    private static final Logger log = LoggerFactory.getLogger(JavetServer.class);

    public static void main(String[] args) {
        File javetScript = FileUtils.getBasePath().resolve("./src/scripts/javet.js").toFile();
        //File catScript = FileUtils.getBasePath().resolve("./src/scripts/javet/cat.js").toFile();
        //File javaModuleScript = FileUtils.getBasePath().resolve("./src/scripts/javet/java_modules.js").toFile();

        try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime();
             JNEventLoop eventLoop = new JNEventLoop(v8Runtime)) {

            JavetProxyConverter javetProxyConverter = new JavetProxyConverter();
            v8Runtime.setConverter(javetProxyConverter);

            //v8Runtime.setV8ModuleResolver(new JavetBuiltInModuleResolver());
            v8Runtime.setLogger(new CustomJavetLogger());
            eventLoop.loadStaticModules(JNModuleType.Console);

            V8ValueObject valueComponent = javetProxyConverter.toV8Value(v8Runtime, Component.class);
            V8ValueObject valueTextComponent = javetProxyConverter.toV8Value(v8Runtime, TextComponent.class);
            V8ValueObject valueTextColor = javetProxyConverter.toV8Value(v8Runtime, TextColor.class);

            V8ValueObject module = v8Runtime.createV8ValueObject();
            module.set("Component", valueComponent);
            module.set("TextComponent", valueTextComponent);
            module.set("TextColor", valueTextColor);

            V8Module adventureModule = v8Runtime.createV8Module("@adventure", module);

            v8Runtime.setV8ModuleResolver((runtime, resourceName, referrer) -> {
                try {
                    if ("@adventure".equals(resourceName)) {
                        return adventureModule;
                    }
                    // Resolve the directory of the referrer module
                    Path referrerPath = referrer != null ? Path.of(referrer.getResourceName()).getParent() : null;

                    // If referrer exists, resolve the relative path
                    if (referrerPath != null) {
                        Path resolvedPath = referrerPath.resolve(resourceName).normalize();
                        if (Files.exists(resolvedPath)) {
                            String moduleSource = Files.readString(resolvedPath);
                            return runtime.getExecutor(moduleSource)
                                    .setResourceName(resolvedPath.toString())
                                    .compileV8Module();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null; // Return null if the module cannot be resolved
            });

            v8Runtime.getExecutor(javetScript).setModule(true).setResourceName(javetScript.getAbsolutePath()).executeVoid();
            //v8Runtime.getExecutor(catScript).setModule(true).setResourceName(catScript.getAbsolutePath()).executeVoid();
            //v8Runtime.getExecutor(javaModuleScript).setModule(true).setResourceName(javaModuleScript.getAbsolutePath()).executeVoid();

            v8Runtime.removeV8Module(adventureModule);
            adventureModule.close();
            module.close();
            valueComponent.close();
            valueTextComponent.close();
            valueTextColor.close();

            eventLoop.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
