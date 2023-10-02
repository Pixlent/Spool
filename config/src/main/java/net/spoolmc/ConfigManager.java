package net.spoolmc;

import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.spoolmc.file.FileManager;
import net.spoolmc.file.Jsone;
import net.spoolmc.logger.Logger;

import java.nio.file.Path;

public class ConfigManager {
    private static final Logger logger = new Logger("ConfigManager");
    @Getter private static final Config config = load();
    @Getter private static final String serverBrand = "Spool";

    public static Config load() {
        final Path configPath = FileManager.getBasePath().resolve("config.json");
        final Jsone jsone = new Jsone("@");

        jsone.addVariable("example_package", "motd", "This is an example motd (from variable)");

        FileManager.saveResourceIfNotExists(configPath, "config.json");
        String configContent = FileManager.readFile(configPath.toFile());

        return jsone.fromJson(configContent, Config.class);
    }

    public static void applyServerConfig() {
        if (System.getProperty("minestom.chunk-view-distance") == null)
            System.setProperty("minestom.chunk-view-distance", String.valueOf(config.chunkViewDistance()));

        if (System.getProperty("minestom.entity-view-distance") == null)
            System.setProperty("minestom.entity-view-distance", String.valueOf(config.entityViewDistance()));

        if (config.onlineMode()) MojangAuth.init();

        setupProxy();

        MinecraftServer.setCompressionThreshold(config.compressionThreshold());
        MinecraftServer.setBrandName(serverBrand);
        MinecraftServer.setDifficulty(config.difficulty());
    }

    private static void setupProxy() {
        switch (config.proxy().getType()) {
            case VELOCITY -> setupVelocity();
            case BUNGEE -> {
                BungeeCordProxy.enable();
                System.out.println("BungeeCord support has been enabled");
            }
            case NONE -> logger.info("No proxy has been enabled");
        }
    }

    private static void setupVelocity() {
        if (config.proxy().getVelocitySecret().isEmpty()) {
            logger.error("Velocity support is enabled but the velocity secret is empty");
            System.exit(1000); // Might as well terminate the server, it won't act as expected otherwise
        } else {
            VelocityProxy.enable(config.proxy().getVelocitySecret());
            logger.setup("Velocity support has been enabled");
        }
    }
}
