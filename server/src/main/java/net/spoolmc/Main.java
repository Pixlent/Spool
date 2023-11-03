package net.spoolmc;

import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.spoolmc.logger.Logger;

public class Main {
    @Getter
    private static PackageManager packageManager;
    private static final Logger logger = new Logger("Main");

    public static void main(String[] args) {

        MinecraftServer minecraftServer = MinecraftServer.init();
        Spool.hook(minecraftServer);

        ConfigManager.applyServerConfig();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        instanceContainer.setGenerator(unit -> {

        });

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();

            String message = String.valueOf(ConfigManager.getConfig().onlineMode());

            player.kick(message);
        });

        minecraftServer.start(ConfigManager.getConfig().ip(), ConfigManager.getConfig().port());

        ScriptingEngine.addDefaultBinding("Player", Player.class);
        ScriptingEngine.addDefaultBinding("Spool", MinecraftServer.class);
        ScriptingEngine.addDefaultBinding("GlobalEventHandler", MinecraftServer.getGlobalEventHandler());

        packageManager = new PackageManager();

        ExecutionTimer timer = new ExecutionTimer();
        packageManager.getPackage("example_package").getScripts().get("load").execute(null);
        logger.info("Execution took " + timer.finished() + "ms");

        registerEvents();
    }

    private static void registerEvents() {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(ServerListPingEvent.class, ServerList::onServerListPing);

    }
}
