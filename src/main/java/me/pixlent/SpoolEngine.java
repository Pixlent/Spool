package me.pixlent;

import me.pixlent.utils.FileUtils;
import me.pixlent.utils.ReflectionUtils;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SpoolEngine {
    public Context context;
    private final Context privligedContext;
    public Value bindings;
    private final String language = "js";

    private final Map<String, Map<String, Object>> repos;
    private final Map<String, String> packages;
    private final Map<Path, Value> exportCache = new HashMap<>();

    private final Value javaTypeFunction;

    public SpoolEngine(@NotNull Context context, Map<String, Map<String, Object>> repos, Map<String, String> packages) {
        this.context = context;
        this.privligedContext = Context.newBuilder(language)
                .allowAllAccess(true)
                .allowExperimentalOptions(true)
                .option("engine.CompileImmediately", "true")  // Enable JIT for speed
                .option("engine.Compilation", "true")      // Enable ahead-of-time (AOT) compilation
                .build();
        this.javaTypeFunction = privligedContext.eval(language, "(type) => {return Java.type(type)}");
        this.repos = repos;
        this.packages = packages;
    }

    public SpoolEngine(Map<String, Map<String, Object>> repos, Map<String, String> packages) {
        this.context = Context.newBuilder(language)
                .allowHostAccess(HostAccess.ALL)
                .allowPolyglotAccess(PolyglotAccess.ALL)
//                .allowHostClassLookup(className -> true)
//                .allowNativeAccess(true)
//                .allowHostClassLoading(true)
//                .allowInnerContextOptions(true)
                .allowExperimentalOptions(true)
                .option("engine.CompileImmediately", "true")  // Enable JIT for speed
                .option("engine.Compilation", "true")      // Enable ahead-of-time (AOT) compilation
                .build();
        this.privligedContext = Context.newBuilder(language)
                .allowAllAccess(true)
                .allowExperimentalOptions(true)
                .option("engine.CompileImmediately", "true")  // Enable JIT for speed
                .option("engine.Compilation", "true")      // Enable ahead-of-time (AOT) compilation
                .build();
        this.javaTypeFunction = privligedContext.eval(language, "(type) => {return Java.type(type)}");
        this.repos = repos;
        this.packages = packages;
    }

    public void execute(File file) {
        try {
            if (exportCache.containsKey(file.toPath())) return;
            String code = FileUtils.readFile(file);

            Value function = context.eval(Source
                    .newBuilder("js", "(exports, require) => {\n" + code + "\n}", file.getName())
                    .buildLiteral());

            Value exports = context.eval("js", "({})");

            if (function.canExecute()) {
                function.execute(exports, makeRequire(file.toPath().getParent()));
            }

            exportCache.put(file.toPath(), exports);
        } catch (PolyglotException e) {
            // Print detailed error information
            if (e.isSyntaxError()) {
                System.err.println(e.getMessage());
            } else {
                if (e.getSourceLocation() != null) {
                    System.err.println("Error in " + file.getPath() + " @" + e.getSourceLocation().getStartLine() +
                            ":" + e.getSourceLocation().getStartColumn() + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Arrays.stream(e.getStackTrace()).forEach(System.err::println);
        }
    }

    public Value execute(String source) {
        return context.eval(Source.create(language, source));
    }

    private Value makeRequire(Path scriptDirectory) {
        return context.asValue((ProxyExecutable) args -> {
            if (args.length == 1) {
                return require(args[0].asString(), scriptDirectory);
            }
            throw new IllegalArgumentException("require() expects exactly one argument.");
        });
    }

    private Value require(String modulePath, Path currentDirectory) {
        if (modulePath.startsWith("@")) {
            return requireFromRepo(modulePath);
        } else {
            return requireFromFile(modulePath, currentDirectory);
        }
    }

    private Value requireFromRepo(String modulePath) {
        modulePath = modulePath.substring(1);
        Value jsObject = context.eval("js", "({})");

        if (modulePath.contains("/")) {
            String[] parts = modulePath.split("/", 2);
            if (parts.length > 1) {
                parts[1] = parts[1].replace("/", ".");
            }

            if (!packages.containsKey(parts[0])) {
                throw new RuntimeException("Package not found: @" + modulePath);
            }
            String packagePath = packages.get(parts[0]);

            List<String> entries = ReflectionUtils.findClassNamesInPackage(packagePath + "." + parts[1]);

            entries.forEach(c -> {
                c = c.replaceFirst(packagePath + "." + parts[1] + ".", "");
                if (!c.contains(".")) {
                    try {
                        jsObject.putMember(c, requireClass(packagePath + "." + parts[1] + "." + c));
                    } catch (Exception _) {}
                }
            });

            return jsObject;
        }

        if (!repos.containsKey(modulePath)) {
            if (!packages.containsKey(modulePath)) {
                throw new RuntimeException("Package not found: @" + modulePath);
            }
            String packagePath = packages.get(modulePath);

            List<String> entries = ReflectionUtils.findClassNamesInPackage(packagePath);

            entries.forEach(c -> {
                c = c.replaceFirst(packagePath + ".", "");
                if (!c.contains(".")) {
                    try {
                        jsObject.putMember(c, requireClass(packagePath + "." + c));
                    } catch (Exception _) {}
                }
            });

            return jsObject;
        }

        Map<String, Object> repoContents = repos.get(modulePath);

        for (Map.Entry<String, Object> entry : repoContents.entrySet()) {
            jsObject.putMember(entry.getKey(), entry.getValue());
        }

        return jsObject;
    }

    public Value requireClass(String type) {
        return javaTypeFunction.execute(type);
    }

    private Value requireFromFile(String exportPath, Path currentDirectory) {
        Path resolvedPath = resolveExportPath(exportPath, currentDirectory);
        if (resolvedPath == null) throw new RuntimeException("Module not found: " + exportPath);

        if (!exportCache.containsKey(resolvedPath)) execute(resolvedPath.toFile());
        return exportCache.get(resolvedPath);
    }

    private Path resolveExportPath(String exportPath, Path currentDirectory) {
        Path relativePath = currentDirectory.resolve(exportPath).normalize();
        if (Files.exists(relativePath)) return relativePath;
        return null;
    }
}
