package me.pixlent.module;

import me.pixlent.SpoolApi;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.nio.file.Files;
import java.nio.file.Path;

public class RequireApi {
    public static Value makeRequire(Path scriptDirectory) {
        return SpoolApi.INSTANCE.privileged.asValue((ProxyExecutable) args -> {
            if (args.length == 1) {
                return require(args[0].asString(), scriptDirectory);
            }
            throw new IllegalArgumentException("require() expects exactly one argument.");
        });
    }

    private static Value require(String modulePath, Path currentDirectory) {
        if (modulePath.startsWith(".") || modulePath.startsWith("/")) {
            Path resolvedPath = resolveExportPath(modulePath, currentDirectory);
            if (resolvedPath == null) throw new RuntimeException("Module not found: " + modulePath);
            if (!SpoolApi.INSTANCE.containsExport(resolvedPath)) {
                SpoolApi.INSTANCE.execute(resolvedPath.toFile());
            }
            return SpoolApi.INSTANCE.fetchExports(resolvedPath);
        }
        if (SpoolApi.INSTANCE.containsModule(modulePath)) {
            return SpoolApi.INSTANCE.fetchModule(modulePath);
        }
        throw new RuntimeException("Module or export not found: " + modulePath);
    }

    private static Path resolveExportPath(String exportPath, Path currentDirectory) {
        Path relativePath = currentDirectory.resolve(exportPath).normalize();
        if (Files.exists(relativePath)) return relativePath;
        return null;
    }
}
