package me.pixlent.api;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.V8Value;
import me.pixlent.Spool;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class JSNative {
    private static final Map<String, Class<?>> classes = new HashMap<>();

    public static void clearCache() {
        classes.clear();
    }

    public static V8Value type(String path) {
        try {
            Class<?> value;
            if (classes.containsKey(path)) {
                value = classes.get(path);
            } else {
                value = loadClass(path);
                classes.put(path, value);
            }
            return Spool.INSTANCE.proxyConverter.toV8Value(Spool.INSTANCE.nodeRuntime, value);
        } catch (JavetException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> loadClass(String name) throws ClassNotFoundException {
        return Class.forName(name, true, getUniversalClassLoader());
    }

    private static ClassLoader getUniversalClassLoader() {
        // Use the current thread's context class loader as the parent
        return new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader()) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                try {
                    // First try parent class loader (standard delegation)
                    return super.loadClass(name);
                } catch (ClassNotFoundException e) {
                    // Fallback to custom loading if parent fails
                    return findClass(name);
                }
            }
        };
    }
}
