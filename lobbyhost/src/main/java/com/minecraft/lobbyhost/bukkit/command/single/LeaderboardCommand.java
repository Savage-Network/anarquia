package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.menu.leaderboard.LeaderboardHubMenu;

public final class LeaderboardCommand implements CommandInheritor {

    @Command(name = "leaderboard", aliases = {"lb", "ranking", "top"}, playerOnly = true,
            description = "Rankings globais (kills, money, clãs...)",
            usage = "/leaderboard")
    public void execute(BukkitCommandContext ctx) {
        new LeaderboardHubMenu(ctx.player()).open();
    }
}
