package net.spoolmc;

import com.google.gson.Gson;
import net.spoolmc.data.Manifest;
import net.spoolmc.logger.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PackageManager {
    private final Logger logger = new Logger("PackageManager");
    private final Map<String, Package> packages = new HashMap<>();

    PackageManager() {
        index();
    }

    public Package getPackage(String id) {
        return packages.get(id);
    }

    private void index() {
        Path packageDirectory = FileManager.getBasePath().resolve("packages");

        if (!validatePackageDirectory(packageDirectory)) return;

        FileManager.searchDirectory(packageDirectory).forEach(file -> validatePackage(file.toPath()));
    }

    /*
     * Returns false if it failed making the package directory
     */
    private boolean validatePackageDirectory(Path packageDirectory) {
        if (packageDirectory.toFile().exists()) return true;
        if (!packageDirectory.toFile().mkdirs()) {
            logger.error("Failed creating package directory");
            return false;
        }
        return true;
    }

    private void validatePackage(Path packageDirectory) {
        final Gson gson = new Gson();

        File manifestFile = packageDirectory.resolve("manifest.json").toFile();

        if (!manifestFile.exists()) {
            logger.error("Manifest.json doesn't exist: " + manifestFile.getParent());
            return;
        }

        Manifest manifest = gson.fromJson(FileManager.readFile(manifestFile), Manifest.class);

        packages.put(manifest.id(), new Package(packageDirectory, manifest, ScriptingEngine.getCompiler()));

        logger.info("Loaded package: " + manifest.id());
    }
}
