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
    private List<Package> packages = new ArrayList<>();

    PackageManager() {
        index();
    }

    private void index() {
        Path packageDirectory = FileManager.getBasePath().resolve("packages");

        if (!validatePackageDirectory(packageDirectory)) return;

        FileManager.searchDirectory(packageDirectory).forEach(file -> {
            validatePackage(file.toPath());
        });
    }

    /*
     * Returns false if it failed making the package directory
     */
    private boolean validatePackageDirectory(Path packageDirectory) {
        if (packageDirectory.toFile().exists()) return true;
        if (!packageDirectory.toFile().mkdirs()) {
            System.out.println("Failed creating package directory");
            return false;
        }
        return true;
    }

    private void validatePackage(Path packageDirectory) {
        final Gson gson = new Gson();

        File manifestFile = packageDirectory.resolve("manifest.json").toFile();

        if (!manifestFile.exists()) {
            Logger.error("PackageManager/Package", "Manifest.json doesn't exist: " + manifestFile.getParent());
            return;
        }

        Manifest manifest = gson.fromJson(FileManager.readFile(manifestFile), Manifest.class);

        System.out.println(manifest.id());
    }
}
