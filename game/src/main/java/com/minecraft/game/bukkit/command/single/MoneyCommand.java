package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class MoneyCommand implements CommandInheritor {

    @Command(name = "money", aliases = {"saldo", "balance", "bal"}, playerOnly = true, usage = "/money [jogador]")
    public void money(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            Account account = ctx.account().orElse(null);
            if (account == null) { ctx.reply("§cConta nao carregada."); return; }
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
            ctx.reply("§cConta nao carregada.");
            return;
        }
        ctx.reply("§7Saldo de §f" + target.getName() + "§7: §a"
                + SemiAnarchyConfig.currencySymbol() + account.getGolds());
    }

    @Command(name = "pay", playerOnly = true, usage = "/pay <jogador> <valor>")
    public void pay(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }
        if (ctx.getArgs().length < 2) {
            ctx.reply("§cUso: /pay <jogador> <valor>");
            return;
        }

        Player target = Bukkit.getPlayerExact(ctx.getArgs()[0]);
        if (target == null) {
            ctx.reply("§cJogador offline.");
            return;
        }
        if (target.equals(ctx.player())) {
            ctx.reply("§cVoce nao pode pagar a si mesmo.");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(ctx.getArgs()[1]);
        } catch (NumberFormatException e) {
            ctx.reply("§cValor invalido.");
            return;
        }
        if (amount <= 0) {
            ctx.reply("§cValor deve ser positivo.");
            return;
        }
        if (!account.removeGolds(amount)) {
            ctx.reply("§cSaldo insuficiente.");
            return;
        }

        Account targetAcc = Core.getAccountController().of(target.getUniqueId());
        if (targetAcc == null) {
            account.addGolds(amount);
            ctx.reply("§cConta do destino nao carregada.");
            return;
        }

        targetAcc.addGolds(amount);
        account.save();
        targetAcc.save();

        String sym = SemiAnarchyConfig.currencySymbol();
        ctx.reply("§aVoce enviou §f" + sym + amount + " §apara §f" + target.getName() + "§a.");
        target.sendMessage("§aVoce recebeu §f" + sym + amount + " §ade §f" + ctx.player().getName() + "§a.");
    }
}
