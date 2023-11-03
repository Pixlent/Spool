package net.spoolmc;

import lombok.Getter;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PackageManager {
    @Getter
    private static PackageManager packageManager = null;
    private final Map<String, Package> packages = new HashMap<>();

    private PackageManager() {

    }

    public static void hook() {
        if (packageManager == null) packageManager = new PackageManager();
    }

    public void registerPackage(Path path) {

    }

}
