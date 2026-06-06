package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.backend.data.AccountData;
import com.minecraft.game.backend.data.NetworkPresenceData;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.menu.stats.StatsMenu;
import com.minecraft.game.network.ActivityFeedService;
import com.minecraft.game.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class StatsCommand implements CommandInheritor {

    @Command(name = "stats", aliases = {"estatisticas", "conquistas"}, playerOnly = true,
            description = "Menu de estatísticas e conquistas",
            usage = "/stats [jogador]")
    public void stats(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            Account self = require(ctx);
            if (self == null) return;
            StatsMenu.open(ctx.player(), self);
            return;
        }
        showPublic(ctx, ctx.getArgs()[0]);
    }

    @Command(name = "perfil", aliases = {"profile"}, playerOnly = true,
            description = "Perfil público de um jogador",
            usage = "/perfil <nick>")
    public void perfil(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            ctx.reply(ModernText.parse(ModernText.hint("perfil <nick>")));
            return;
        }
        showPublic(ctx, ctx.getArgs()[0]);
    }

    private void showPublic(BukkitCommandContext ctx, String nick) {
        Player viewer = ctx.player();
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            AccountData data = Core.getAccountData();
            if (data == null) {
                Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () ->
                        ctx.reply(ModernText.parse("<!italic><red>Sistema indisponível.")));
                return;
            }
            var loadedOpt = data.loadByName(nick);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                if (loadedOpt.isEmpty()) {
                    ctx.reply(ModernText.parse("<!italic><red>Jogador não encontrado."));
                    return;
                }
                Account loaded = loadedOpt.get();
                if (!loaded.getToggle().isShowStats() && !viewer.hasPermission("game.admin")) {
                    ctx.reply(ModernText.parse("<!italic><yellow>Este jogador ocultou as estatísticas."));
                    return;
                }
                var stats = loaded.getGameStats();
                var progress = loaded.getPlayerProgress();
                String clanLine = loaded.hasClan() && loaded.getClan() != null
                        ? loaded.getClan().getName() + " [" + loaded.getClan().getTag() + "]"
                        : "Nenhum";
                Optional<NetworkPresenceData.Presence> presence = Core.getNetworkPresenceData() != null
                        ? Core.getNetworkPresenceData().findOnline(loaded.getId()) : Optional.empty();
                String status = presence.map(p -> "§aOnline em §f" + formatServer(p.serverId()))
                        .orElse("§cOffline");

                ctx.reply(ModernText.parse(ModernText.gradient(loaded.getNickname(), "#38BDF8", "#818CF8")));
                ctx.reply(ModernText.parse("§7Rank: §f" + loaded.getRank().getType().name()
                        + " §7| Tag: §f" + loaded.getTag().name()));
                ctx.reply(ModernText.parse("§7Clã: §f" + clanLine));
                ctx.reply(ModernText.parse("§7Status: " + status));
                ctx.reply(ModernText.parse("§7K/D: §a" + stats.getKills() + "§7/§c" + stats.getDeaths()
                        + " §8(§e" + String.format("%.2f", loaded.getKdr()) + "§8)"));
                ctx.reply(ModernText.parse("§7Streak login: §e" + progress.getLoginStreak()
                        + " §7| Conquistas: §6"
                        + (progress.getAchievements() != null ? progress.getAchievements().size() : 0)));
                ctx.reply(ModernText.parse("§8Use §f/stats §8para abrir seu menu."));
            });
        });
    }

    private static String formatServer(String serverId) {
        return switch (serverId != null ? serverId.toLowerCase() : "") {
            case "game" -> "Semi-Anarquia";
            case "lobby", "lobbyhost" -> "Lobby";
            case "eventos" -> "Eventos";
            default -> serverId != null ? serverId : "?";
        };
    }

    private Account require(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) ctx.replyKey("errors.account-not-loaded");
        return account;
    }
}
