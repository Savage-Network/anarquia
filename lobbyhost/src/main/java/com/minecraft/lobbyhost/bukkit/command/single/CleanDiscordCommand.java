package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.display.DiscordHologramManager;

public final class CleanDiscordCommand implements CommandInheritor {

    @Command(name = "cleandiscord", permission = "lobbyhost.admin",
            description = "Remove o holograma do Discord")
    public void execute(BukkitCommandContext ctx) {
        DiscordHologramManager.cleanup();
        ctx.reply("§eHolograma do Discord removido.");
    }
}
