package net.spoolmc;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.server.ServerListPingEvent;

public class Main {
    private static PackageManager packageManager;

    public static void main(String[] args) {

        MinecraftServer minecraftServer = MinecraftServer.init();
        ConfigManager.applyServerConfig();

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();

            String message = String.valueOf(ConfigManager.getConfig().onlineMode());

            player.kick(message);
        });

        packageManager = new PackageManager();

        minecraftServer.start(ConfigManager.getConfig().ip(), ConfigManager.getConfig().port());

    }

    private static void registerEvents() {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(ServerListPingEvent.class, ServerList::onServerListPing);


    }
}
