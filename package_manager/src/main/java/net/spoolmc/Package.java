package net.spoolmc;

import lombok.Getter;
import net.spoolmc.data.Manifest;
import net.spoolmc.logger.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Package {
            private final Logger       logger  = new Logger("Package");
            private final Compiler compiler;
    @Getter private final Map<String, Script> scripts = new HashMap<>();
    @Getter private final Manifest     manifest;
    @Getter private final Path         packageDirectory;

    Package(Path packageDirectory, Manifest manifest, Compiler compiler) {
        this.packageDirectory = packageDirectory;
        this.manifest = manifest;
        this.compiler = compiler;
        index(packageDirectory);
    }

    private void index(Path directory) {
        Path scriptDirectory = directory.resolve("src/scripts");

        if (!scriptDirectory.toFile().exists()) {
            logger.info("No scripts: " + manifest.id() + " Path: " + scriptDirectory);
            return;
        }

        indexScripts(scriptDirectory);
    }

    private void indexScripts(Path scriptDirectory) {
        FileManager.searchDirectoryDeep(scriptDirectory).forEach(file -> {
            if (file.getName().endsWith(".js")) {
                scripts.put(file.getName().replace(".js", ""), new Script(file, compiler));
                logger.info("Loaded script: " + file.getName());
            }
        });
    }
}
