package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.menu.leaderboard.LeaderboardHubMenu;
import com.minecraft.game.bukkit.menu.leaderboard.LeaderboardListMenu;
import com.minecraft.game.leaderboard.LeaderboardType;
import com.minecraft.game.util.TabCompleteUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class TopCommand implements CommandInheritor {

    @Command(name = "top", aliases = {"ranking", "leaderboard", "rankings"},
            usage = "/leaderboard [kills|deaths|kdr|money|clans]")
    public void execute(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            new LeaderboardHubMenu(ctx.player()).open();
            return;
        }

        LeaderboardType type = LeaderboardType.fromArg(ctx.getArgs()[0]);
        if (type == null) {
            ctx.reply("§cCategoria invalida. Use: kills, deaths, kdr, money, clans");
            return;
        }
        new LeaderboardListMenu(ctx.player(), type, new LeaderboardHubMenu(ctx.player())).open();
    }

    @Completer(name = "top", subCommands = {"ranking", "leaderboard", "rankings"})
    public List<String> tab(BukkitCommandContext ctx) {
        List<String> opts = TabCompleteUtil.leaderboardTypes();
        String partial = ctx.getArgs().length > 0 ? ctx.getArgs()[0].toLowerCase(Locale.ROOT) : "";
        return opts.stream().filter(o -> o.startsWith(partial)).collect(Collectors.toList());
    }
}
