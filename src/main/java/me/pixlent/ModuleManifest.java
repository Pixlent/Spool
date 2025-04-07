package me.pixlent;

import com.google.gson.annotations.SerializedName;

public record ModuleManifest(int format, ModuleHeader header, ModuleDependency[] dependencies) {
    public record ModuleDependency(
            String name,
            int[] min_version,
            String repository,
            String checksum
    ) {}
    public record ModuleHeader(
            String name,
            String description,
            int[] version,
            @SerializedName("min_engine_version") int[] minEngineVersion,
            @SerializedName("entry_points") String[] entryPoints
    ) {}
}
