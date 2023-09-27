package net.spoolmc;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;

public class Main {

    public static void main(String[] args) {

        MinecraftServer minecraftServer = MinecraftServer.init();
        ConfigManager.applyServerConfig();

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();

            String message = String.valueOf(ConfigManager.getConfig().onlineMode());

            player.kick(message);
        });

        minecraftServer.start(ConfigManager.getConfig().ip(), ConfigManager.getConfig().port());

    }
}
