package net.spoolmc;

import com.google.gson.Gson;
import net.spoolmc.data.Manifest;
import net.spoolmc.file.FileManager;
import net.spoolmc.logger.Logger;

import java.io.File;
import java.nio.file.Path;

public class Package {
    private final Manifest manifest;
    private final Path packageDirectory;
    Package(Path packageDirectory) {
        final Gson gson = new Gson();

        File manifest = packageDirectory.resolve("manifest.json").toFile();

        if (!manifest.exists()) {
            Logger.error("PackageManager/Package", "Manifest.json doesn't exist: " + manifest.getParent());
        }

        this.packageDirectory = packageDirectory;
        this.manifest = gson.fromJson(FileManager.readFile(manifest), Manifest.class);
    }
}
