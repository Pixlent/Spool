package me.pixlent;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.callback.JavetBuiltInModuleResolver;
import com.caoccao.javet.values.reference.IV8Module;
import com.caoccao.javet.values.reference.V8Module;

import java.nio.file.Files;
import java.nio.file.Path;

public class VirtualModuleResolver extends JavetBuiltInModuleResolver {

    public VirtualModuleResolver() {
    }

    public V8Module resolve(V8Runtime runtime, String resourceName, IV8Module referrer) throws JavetException {
        try {
            // Resolve the directory of the referrer module
            Path referrerPath = referrer != null ? Path.of(referrer.getResourceName()).getParent() : null;

            // If referrer exists, resolve the relative path
            if (referrerPath != null) {
                Path resolvedPath = referrerPath.resolve(resourceName).normalize();
                //System.out.println(resolvedPath);
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
    }
}
