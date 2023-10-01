package net.spoolmc;

import com.google.gson.Gson;
import net.spoolmc.data.Manifest;
import net.spoolmc.file.FileManager;
import net.spoolmc.logger.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PackageManager {
    private final Logger logger = new Logger("PackageManager");
    private List<Package> packages = new ArrayList<>();

    PackageManager() {
        index();
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

        packages.add(new Package(packageDirectory, manifest));

        logger.info("Loaded package: " + manifest.id());
    }
}
