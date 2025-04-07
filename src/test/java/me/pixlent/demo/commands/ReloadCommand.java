package me.pixlent.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.command.builder.Command;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload");

        setDefaultExecutor((sender, _) -> {
            System.out.println("Implement reload command");
            sender.sendMessage(Component.text("Reloaded!").color(TextColor.fromHexString("#fff066")));
        });
    }
}
