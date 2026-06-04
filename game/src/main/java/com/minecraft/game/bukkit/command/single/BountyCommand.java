package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class BountyCommand implements CommandInheritor {

    @Command(name = "bounty", aliases = {"recompensa", "caçada"}, playerOnly = true,
            usage = "/bounty <jogador> [valor]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }
        if (ctx.getArgs().length < 1) {
            ctx.reply("§cUso: /bounty <jogador> [valor]");
            return;
        }

        Player target = Bukkit.getPlayerExact(ctx.getArgs()[0]);
        if (target == null) {
            ctx.reply("§cJogador offline.");
            return;
        }
        if (target.equals(ctx.player())) {
            ctx.reply("§cVoce nao pode colocar bounty em si mesmo.");
            return;
        }

        Account targetAcc = Core.getAccountController().of(target.getUniqueId());
        if (targetAcc == null) {
            ctx.reply("§cConta do alvo nao carregada.");
            return;
        }

        if (ctx.getArgs().length == 1) {
            int current = targetAcc.getGameStats().getBounty();
            ctx.reply(current > 0
                    ? "§7Bounty em §f" + target.getName() + "§7: §a"
                    + SemiAnarchyConfig.currencySymbol() + current
                    : "§7Nenhuma bounty ativa em §f" + target.getName() + "§7.");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(ctx.getArgs()[1]);
        } catch (NumberFormatException e) {
            ctx.reply("§cValor invalido.");
            return;
        }
        if (amount < SemiAnarchyConfig.bountyMin()) {
            ctx.reply("§cMinimo: " + SemiAnarchyConfig.currencySymbol() + SemiAnarchyConfig.bountyMin());
            return;
        }

        int tax = (amount * SemiAnarchyConfig.bountyTaxPercent()) / 100;
        int total = amount + tax;
        if (!account.removeGolds(total)) {
            ctx.reply("§cSaldo insuficiente (valor + taxa " + SemiAnarchyConfig.bountyTaxPercent() + "%).");
            return;
        }

        targetAcc.getGameStats().setBounty(targetAcc.getGameStats().getBounty() + amount);
        account.markDirty();
        targetAcc.markDirty();

        Bukkit.broadcast(net.kyori.adventure.text.Component.text(
                "§6§lBOUNTY §f" + ctx.player().getName() + " §7colocou §a"
                        + SemiAnarchyConfig.currencySymbol() + amount + " §7na cabeca de §f"
                        + target.getName() + "§7!"));
        ctx.reply("§aBounty aplicada. Taxa: §f" + SemiAnarchyConfig.currencySymbol() + tax);
    }
}
