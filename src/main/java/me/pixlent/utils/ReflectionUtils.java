package me.pixlent.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ReflectionUtils {
    public static List<String> findClassNamesInPackage(String packageName) {
        List<String> classNames = new ArrayList<>();
        Guard.tryCatch("IOException", () -> {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource != null) {
                    String protocol = resource.getProtocol();
                    if ("file".equals(protocol)) {
                        findClassNamesInDirectory(new File(resource.getFile()), packageName, classNames);
                    } else if ("jar".equals(protocol)) {
                        findClassNamesInJar(resource, packageName, classNames);
                    }
                }
            }
        });
        return classNames;
    }

    private static void findClassNamesInDirectory(File directory, String packageName, List<String> classNames) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class") && !file.getName().contains("$")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    classNames.add(className);
                }
            }
        }
    }

    private static void findClassNamesInJar(URL resource, String packageName, List<String> classNames) throws IOException {
        String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
        try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath)) {
            Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();
            String packagePath = packageName.replace('.', '/');

            while (entries.hasMoreElements()) {
                java.util.jar.JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(packagePath) && entryName.endsWith(".class") && !entryName.contains("$")) {
                    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                    classNames.add(className);
                }
            }
        }
    }
}
