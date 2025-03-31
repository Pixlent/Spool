package me.pixlent.demo.commands;

import me.pixlent.Spool;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.command.builder.Command;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload");

        setDefaultExecutor((sender, _) -> {
            Spool.hook().reload();
            sender.sendMessage(Component.text("Reloaded!").color(TextColor.fromHexString("#fff066")));
        });
    }
}
