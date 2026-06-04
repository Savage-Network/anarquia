package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class TopCommand implements CommandInheritor {

    @Command(name = "top", aliases = {"ranking", "leaderboard"}, usage = "/top <kills|deaths|kdr|money>")
    public void execute(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            ctx.reply("§cUso: /top <kills|deaths|kdr|money>");
            return;
        }

        String mode = ctx.getArgs()[0].toLowerCase(Locale.ROOT);
        List<Account> accounts = new ArrayList<>(Core.getAccountController().list());
        if (accounts.isEmpty()) {
            ctx.reply("§7Nenhum jogador online para ranking.");
            return;
        }

        Comparator<Account> cmp = switch (mode) {
            case "deaths" -> Comparator.comparingInt(a -> a.getGameStats().getDeaths());
            case "money", "golds", "saldo" -> Comparator.comparingInt(Account::getGolds);
            case "kdr" -> Comparator.comparingDouble(Account::getKdr);
            default -> Comparator.comparingInt(a -> a.getGameStats().getKills());
        };

        accounts.sort(cmp.reversed());
        ctx.reply("§6§lTOP §7" + mode.toUpperCase(Locale.ROOT) + " §8(jogadores online)");
        int pos = 1;
        for (Account a : accounts.stream().limit(10).toList()) {
            String line = switch (mode) {
                case "deaths" -> "§c" + a.getGameStats().getDeaths() + " mortes";
                case "money", "golds", "saldo" -> "§a" + SemiAnarchyConfig.currencySymbol() + a.getGolds();
                case "kdr" -> String.format("§e%.2f KDR §7(%d/%d)",
                        a.getKdr(), a.getGameStats().getKills(), a.getGameStats().getDeaths());
                default -> "§a" + a.getGameStats().getKills() + " kills §7(streak "
                        + a.getGameStats().getKillStreak() + ")";
            };
            ctx.reply("§8" + pos++ + ". §f" + a.getName() + " §7- " + line);
        }
    }
}
