package net.spoolmc.data;

import com.google.gson.annotations.SerializedName;

public record ManifestDependency(
        String id,
        @SerializedName("min_version") String minVersion,
        Boolean depend
) {}
