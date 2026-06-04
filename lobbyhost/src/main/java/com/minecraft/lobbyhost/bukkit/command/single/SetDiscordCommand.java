package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.display.DiscordHologramManager;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public final class SetDiscordCommand implements CommandInheritor {

    @Command(name = "setdiscord", playerOnly = true, permission = "lobbyhost.admin",
            description = "Define a posicao atual como holograma do Discord")
    public void execute(BukkitCommandContext ctx) {
        Player player = ctx.player();
        Location loc = player.getLocation();
        FileConfiguration cfg = LobbyHostPlugin.getInstance().getConfig();
        cfg.set("discord.hologram.enabled", true);
        cfg.set("discord.hologram.world", loc.getWorld().getName());
        cfg.set("discord.hologram.x", loc.getX());
        cfg.set("discord.hologram.y", loc.getY());
        cfg.set("discord.hologram.z", loc.getZ());
        LobbyHostPlugin.getInstance().saveConfig();
        DiscordHologramManager.initialize();
        ctx.reply("§aHolograma do Discord posicionado em "
                + String.format("(%.1f, %.1f, %.1f)", loc.getX(), loc.getY(), loc.getZ()));
    }
}
