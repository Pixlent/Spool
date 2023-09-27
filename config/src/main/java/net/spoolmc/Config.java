package net.spoolmc;

import com.google.gson.annotations.SerializedName;
import net.minestom.server.world.Difficulty;
import net.spoolmc.options.Proxy;

public record Config (
        String ip,
        int port,
        @SerializedName("online-mode") boolean onlineMode,
        @SerializedName("chunk-view-distance") int chunkViewDistance,
        @SerializedName("entity-view-distance") int entityViewDistance,
        @SerializedName("compression-threshold") int compressionThreshold,
        Difficulty difficulty,
        Proxy proxy
) {}
