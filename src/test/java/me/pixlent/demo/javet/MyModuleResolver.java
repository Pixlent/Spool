package me.pixlent.demo.javet;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.callback.JavetBuiltInModuleResolver;
import com.caoccao.javet.values.reference.IV8Module;
import com.caoccao.javet.values.reference.V8Module;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class MyModuleResolver extends JavetBuiltInModuleResolver {
    private final Path rootPath;

    public MyModuleResolver(Path rootPath) {
        this.rootPath = Objects.requireNonNull(rootPath);
    }

    public Path getRootPath() {
        return rootPath;
    }

    public V8Module resolve(V8Runtime v8Runtime, String resourceName, IV8Module v8ModuleReferrer) throws JavetException {
        Path relativeRootPath = rootPath;
        if (v8ModuleReferrer != null) {
            relativeRootPath = Paths.get(v8ModuleReferrer.getResourceName()).getParent();
        }

        Path resourcePath = relativeRootPath.resolve(resourceName).normalize();
        if (resourcePath.toFile().exists()) {
            try {
                String codeString = Files.readString(resourcePath, StandardCharsets.UTF_8);
                return v8Runtime.getExecutor(codeString)
                        .setResourceName(resourcePath.toString())
                        .setModule(true)
                        .compileV8Module();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }
}