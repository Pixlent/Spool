package net.spoolmc;

import lombok.Getter;
import net.minestom.server.MinecraftServer;

public class Spool {
    private static Spool spool = null;
    @Getter private final MinecraftServer server;

    private Spool(MinecraftServer server) {
        this.server = server;
    }

    public static void hook(MinecraftServer server) {
        if (spool == null) spool = new Spool(server);
    }
}
