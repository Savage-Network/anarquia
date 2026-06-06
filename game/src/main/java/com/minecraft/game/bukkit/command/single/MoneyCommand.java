package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import com.minecraft.game.util.TabCompleteUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public final class MoneyCommand implements CommandInheritor {

    @Command(name = "money", aliases = {"saldo", "balance", "bal"}, playerOnly = true, usage = "/money [jogador]")
    public void money(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            Account account = ctx.account().orElse(null);
            if (account == null) { ctx.replyKey("errors.account-not-loaded"); return; }
            ctx.reply("§7Saldo: §a" + SemiAnarchyConfig.currencySymbol() + account.getGolds());
            return;
        }

        Player target = Bukkit.getPlayerExact(ctx.getArgs()[0]);
        if (target == null) {
            ctx.reply("§cJogador offline.");
            return;
        }
        Account account = Core.getAccountController().of(target.getUniqueId());
        if (account == null) {
            ctx.replyKey("errors.account-not-loaded");
            return;
        }
        ctx.reply("§7Saldo de §f" + target.getName() + "§7: §a"
                + SemiAnarchyConfig.currencySymbol() + account.getGolds());
    }

    @Command(name = "moneygive", aliases = {"givemoney", "addmoney"}, playerOnly = false,
            rank = RankType.ADMIN, permission = "game.money.give",
            usage = "/moneygive <jogador> <valor>", description = "Dá dinheiro a um jogador")
    public void give(BukkitCommandContext ctx) {
        if (ctx.getArgs().length < 2) {
            ctx.reply("§cUso: /moneygive <jogador> <valor>");
            return;
        }
        Player target = Bukkit.getPlayerExact(ctx.getArgs()[0]);
        if (target == null) { ctx.reply("§cJogador offline."); return; }

        Account targetAcc = Core.getAccountController().of(target.getUniqueId());
        if (targetAcc == null) { ctx.replyKey("errors.account-not-loaded"); return; }

        int amount;
        try {
            amount = Integer.parseInt(ctx.getArgs()[1]);
        } catch (NumberFormatException e) {
            ctx.reply("§cValor inválido.");
            return;
        }
        if (amount <= 0) { ctx.reply("§cValor deve ser positivo."); return; }

        targetAcc.addGolds(amount);
        targetAcc.save();

        String sym = SemiAnarchyConfig.currencySymbol();
        ctx.reply("§aAdicionado §f" + sym + amount + " §apara §f" + target.getName() + "§a.");
        target.sendMessage("§aVocê recebeu §f" + sym + amount + " §ado staff.");
    }

    @Command(name = "moneytake", aliases = {"takemoney", "removemoney"}, playerOnly = false,
            rank = RankType.ADMIN, permission = "game.money.take",
            usage = "/moneytake <jogador> <valor>", description = "Remove dinheiro de um jogador")
    public void take(BukkitCommandContext ctx) {
        if (ctx.getArgs().length < 2) {
            ctx.reply("§cUso: /moneytake <jogador> <valor>");
            return;
        }
        Player target = Bukkit.getPlayerExact(ctx.getArgs()[0]);
        if (target == null) { ctx.reply("§cJogador offline."); return; }

        Account targetAcc = Core.getAccountController().of(target.getUniqueId());
        if (targetAcc == null) { ctx.replyKey("errors.account-not-loaded"); return; }

        int amount;
        try {
            amount = Integer.parseInt(ctx.getArgs()[1]);
        } catch (NumberFormatException e) {
            ctx.reply("§cValor inválido.");
            return;
        }
        if (amount <= 0) { ctx.reply("§cValor deve ser positivo."); return; }

        targetAcc.removeGolds(amount);
        targetAcc.save();

        String sym = SemiAnarchyConfig.currencySymbol();
        ctx.reply("§cRemovido §f" + sym + amount + " §cde §f" + target.getName() + "§c.");
        target.sendMessage("§cO staff removeu §f" + sym + amount + " §cda sua conta.");
    }

    @Completer(name = "money", subCommands = {"saldo", "balance", "bal"})
    public List<String> tabMoney(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) return TabCompleteUtil.onlinePlayers(ctx.getArgs()[0]);
        return List.of();
    }

    @Completer(name = "moneygive", subCommands = {"givemoney", "addmoney"})
    public List<String> tabMoneyGive(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) return TabCompleteUtil.onlinePlayers(ctx.getArgs()[0]);
        if (ctx.getArgs().length == 2) return TabCompleteUtil.filter(ctx.getArgs()[1], "100", "500", "1000", "5000");
        return List.of();
    }

    @Completer(name = "moneytake", subCommands = {"takemoney", "removemoney"})
    public List<String> tabMoneyTake(BukkitCommandContext ctx) {
        return tabMoneyGive(ctx);
    }
}
