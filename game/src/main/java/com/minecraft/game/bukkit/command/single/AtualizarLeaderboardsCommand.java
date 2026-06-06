package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Core;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.hologram.LeaderboardHologramManager;
import com.minecraft.game.leaderboard.LeaderboardCache;
import com.minecraft.game.message.Messages;
import org.bukkit.Bukkit;

import java.util.Map;

public final class AtualizarLeaderboardsCommand implements CommandInheritor {

    @Command(name = "atualizarleaderboards",
            aliases = {"updateleaderboards", "refreshleaderboards", "atualizartop", "atualizarranking"},
            permission = "game.admin",
            description = "Atualiza cache dos rankings e hologramas",
            usage = "/atualizarleaderboards")
    public void execute(BukkitCommandContext ctx) {
        ctx.replyKey("leaderboard.refresh-start");
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            LeaderboardCache.refreshAll();
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                LeaderboardHologramManager.refreshDisplays();
                if (ctx.isPlayer()) {
                    ctx.player().sendMessage(Messages.component("leaderboard.refresh-done", ctx.lang(),
                            Map.of("count", String.valueOf(LeaderboardHologramManager.activeCount()))));
                } else {
                    ctx.getSender().sendMessage(Messages.component("leaderboard.refresh-done", ctx.lang(),
                            Map.of("count", String.valueOf(LeaderboardHologramManager.activeCount()))));
                }
            });
        });
    }
}
