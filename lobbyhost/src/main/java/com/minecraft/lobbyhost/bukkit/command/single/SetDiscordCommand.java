package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.display.DiscordHologramManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class SetDiscordCommand implements CommandInheritor {

    @Command(name = "setdiscord", playerOnly = true, permission = "lobbyhost.admin",
            description = "Define a posicao atual como holograma do Discord")
    public void execute(BukkitCommandContext ctx) {
        Player player = ctx.player();
        Location loc = player.getLocation();
        DiscordHologramManager.savePosition(loc);
        DiscordHologramManager.initialize();
        ctx.reply("§aHolograma do Discord posicionado em "
                + String.format("(%.1f, %.1f, %.1f)", loc.getX(), loc.getY(), loc.getZ()));
    }
}
