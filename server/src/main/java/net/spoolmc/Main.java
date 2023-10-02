package net.spoolmc;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.spoolmc.logger.Logger;

public class Main {
    private static PackageManager packageManager;
    private static ScriptingEngine scriptingEngine;
    private static final Logger logger = new Logger("Main");

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
        scriptingEngine = new ScriptingEngine();

        minecraftServer.start(ConfigManager.getConfig().ip(), ConfigManager.getConfig().port());

        logger.info("motd: " + ConfigManager.getConfig().motd());

        registerEvents();
    }

    private static void registerEvents() {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(ServerListPingEvent.class, ServerList::onServerListPing);

    }
}
