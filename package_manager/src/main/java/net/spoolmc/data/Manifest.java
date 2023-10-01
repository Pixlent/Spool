package net.spoolmc.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record Manifest (
        String id,
        String version,
        @SerializedName("min_api_version") String minApiVersion,
        ManifestDisplay display,
        List<ManifestDependency> dependencies
) {}
