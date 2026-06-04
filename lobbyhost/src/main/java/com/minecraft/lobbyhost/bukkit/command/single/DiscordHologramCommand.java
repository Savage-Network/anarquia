package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.display.DiscordHologramManager;

public final class DiscordHologramCommand implements CommandInheritor {

    @Command(name = "discordhologram", permission = "lobbyhost.admin",
            description = "Recarrega o holograma do Discord")
    public void execute(BukkitCommandContext ctx) {
        DiscordHologramManager.initialize();
        ctx.reply("§aHolograma do Discord recarregado.");
    }
}
