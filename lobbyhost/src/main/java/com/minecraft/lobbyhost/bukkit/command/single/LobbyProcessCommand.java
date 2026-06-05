package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.config.DiscordConfig;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.display.DiscordHologramManager;
import com.minecraft.lobbyhost.display.npc.NpcManager;

public final class LobbyProcessCommand implements CommandInheritor {

    @Command(name = "lobbyprocess", aliases = {"lobbyadmin"}, permission = "lobbyhost.admin",
            description = "Subcomandos administrativos do lobby",
            usage = "/lobbyprocess <reload|reloadArchitect|npcReload|discordReload>")
    public void execute(BukkitCommandContext ctx) {
        String sub = ctx.getArgs().length > 0 ? ctx.getArgs()[0].toLowerCase() : "help";

        switch (sub) {
            case "reload" -> {
                LobbyHostPlugin.getInstance().reloadAll();
                ctx.reply("§aConfig.yml recarregado.");
            }
            case "reloadarchitect" -> {
                LobbyHostPlugin.getInstance().getArchitectLoader().unload();
                LobbyHostPlugin.getInstance().getArchitectLoader().load();
                LobbyHostPlugin.getInstance().getArchitectLoader().getArchitect().load();
                ctx.reply("§aArchitect recarregado.");
            }
            case "npcreload" -> {
                NpcManager.load(LobbyHostPlugin.getInstance());
                LobbyHostPlugin.getInstance().getArchitectLoader().getArchitect().load();
                ctx.reply("§aNPCs recarregados.");
            }
            case "discordreload" -> {
                DiscordConfig.load(LobbyHostPlugin.getInstance());
                DiscordHologramManager.initialize();
                ctx.reply("§aDiscord recarregado (discord.yml + holograma).");
            }
            default -> ctx.reply("§eUso: /lobbyprocess <reload|reloadArchitect|npcReload|discordReload>");
        }
    }
}
