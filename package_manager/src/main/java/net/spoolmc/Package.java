package net.spoolmc;

import lombok.Getter;
import net.spoolmc.data.Manifest;
import net.spoolmc.file.FileManager;
import net.spoolmc.logger.Logger;
import org.graalvm.polyglot.Context;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Package {
            private final Logger       logger  = new Logger("Package");
    @Getter private final List<Script> scripts = new ArrayList<>();
    @Getter private final Manifest     manifest;
    @Getter private final Path         packageDirectory;

    Package(Path packageDirectory, Manifest manifest) {
        this.packageDirectory = packageDirectory;
        this.manifest = manifest;
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
                // add the script function thingie
                // scripts.add(new Script(file, Context.create()));
                logger.info("Loaded script: " + file.getName());
            }
        });
    }
}
