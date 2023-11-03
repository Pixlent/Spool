package net.spoolmc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.utils.identity.NamedAndIdentified;

import java.nio.file.Path;

public class ServerList {

    public static void onServerListPing(ServerListPingEvent event) {
        ResponseData data = event.getResponseData();
        int onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers().size();

        data.setOnline(onlinePlayers);
        data.setMaxPlayer(onlinePlayers + 1);
        data.setVersion("1.20.1");
        data.setProtocol(763);
        data.setDescription(Component.text("This is the server MOTD\nSecond Line"));
        data.addEntry(NamedAndIdentified.named(Component.text("SpoolMC Spool").color(TextColor.color(202, 177, 222))));

        Base64Encoder encoder = new Base64Encoder();
        Path imagePath = FileManager.getBasePath().resolve("icon.png");
        FileManager.saveResourceIfNotExists(imagePath, "icon.png");



        data.setFavicon("data:image/png;base64," + encoder.encodeToBase64(imagePath.toFile()));

        event.setResponseData(data);
    }

}