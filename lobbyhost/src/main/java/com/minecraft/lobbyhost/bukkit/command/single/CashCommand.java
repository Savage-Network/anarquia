package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.util.AccountSync;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyScoreboard;
import com.minecraft.lobbyhost.util.TabCompleteUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/** Cash de rede (lobby) — separado do money/golds do semi-anarquia. */
public final class CashCommand implements CommandInheritor {

    private static String sym() {
        return LobbySettings.CASH_SYMBOL;
    }

    @Command(name = "cash", aliases = {"coins", "moedas"}, playerOnly = true,
            description = "Consulta seu cash de rede",
            usage = "/cash [jogador]")
    public void cash(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            Account account = ctx.account().orElse(null);
            if (account == null) { ctx.replyKey("errors.account-not-loaded"); return; }
            ctx.reply("§7Cash: §a" + sym() + account.getCash());
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
        ctx.reply("§7Cash de §f" + target.getName() + "§7: §a" + sym() + account.getCash());
    }

    @Command(name = "cashgive", aliases = {"givecash", "addcash"}, playerOnly = false,
            rank = RankType.ADMIN, permission = "lobbyhost.cash.give",
            usage = "/cashgive <jogador> <valor>", description = "Da cash de rede a um jogador")
    public void give(BukkitCommandContext ctx) {
        if (ctx.getArgs().length < 2) {
            ctx.reply("§cUso: /cashgive <jogador> <valor>");
            return;
        }
        Player target = Bukkit.getPlayerExact(ctx.getArgs()[0]);
        if (target == null) { ctx.reply("§cJogador offline."); return; }

        Account targetAcc = Core.getAccountController().of(target.getUniqueId());
        if (targetAcc == null) { ctx.replyKey("errors.account-not-loaded"); return; }

        int amount = parseAmount(ctx, 1);
        if (amount <= 0) return;

        targetAcc.addCash(amount);
        AccountSync.saveAndBroadcast(targetAcc);
        LobbyScoreboard.refreshCash(target);

        ctx.reply("§aAdicionado §f" + sym() + amount + " §ade cash para §f" + target.getName() + "§a.");
        target.sendMessage("§aVoce recebeu §f" + sym() + amount + " §ade cash.");
    }

    @Command(name = "cashtake", aliases = {"takecash", "removecash"}, playerOnly = false,
            rank = RankType.ADMIN, permission = "lobbyhost.cash.take",
            usage = "/cashtake <jogador> <valor>", description = "Remove cash de rede")
    public void take(BukkitCommandContext ctx) {
        if (ctx.getArgs().length < 2) {
            ctx.reply("§cUso: /cashtake <jogador> <valor>");
            return;
        }
        Player target = Bukkit.getPlayerExact(ctx.getArgs()[0]);
        if (target == null) { ctx.reply("§cJogador offline."); return; }

        Account targetAcc = Core.getAccountController().of(target.getUniqueId());
        if (targetAcc == null) { ctx.replyKey("errors.account-not-loaded"); return; }

        int amount = parseAmount(ctx, 1);
        if (amount <= 0) return;

        if (!targetAcc.removeCash(amount)) {
            ctx.reply("§cSaldo insuficiente.");
            return;
        }
        AccountSync.saveAndBroadcast(targetAcc);
        LobbyScoreboard.refreshCash(target);

        ctx.reply("§cRemovido §f" + sym() + amount + " §cde cash de §f" + target.getName() + "§c.");
        target.sendMessage("§cO staff removeu §f" + sym() + amount + " §cde seu cash.");
    }

    @Command(name = "cashpay", aliases = {"paycash", "enviarcash"}, playerOnly = true,
            description = "Envia cash para outro jogador online",
            usage = "/cashpay <jogador> <valor>")
    public void pay(BukkitCommandContext ctx) {
        Account sender = ctx.account().orElse(null);
        if (sender == null) { ctx.replyKey("errors.account-not-loaded"); return; }
        if (ctx.getArgs().length < 2) {
            ctx.reply("§cUso: /cashpay <jogador> <valor>");
            return;
        }

        Player targetPlayer = Bukkit.getPlayerExact(ctx.getArgs()[0]);
        if (targetPlayer == null) { ctx.reply("§cJogador offline."); return; }
        if (targetPlayer.equals(ctx.player())) {
            ctx.reply("§cVoce nao pode enviar cash para si mesmo.");
            return;
        }

        Account target = Core.getAccountController().of(targetPlayer.getUniqueId());
        if (target == null) { ctx.reply("§cConta do alvo nao carregada."); return; }

        int amount = parseAmount(ctx, 1);
        if (amount <= 0) return;

        if (!sender.removeCash(amount)) {
            ctx.reply("§cCash insuficiente.");
            return;
        }
        target.addCash(amount);
        AccountSync.saveAndBroadcast(sender);
        AccountSync.saveAndBroadcast(target);
        LobbyScoreboard.refreshCash(ctx.player());
        LobbyScoreboard.refreshCash(targetPlayer);

        ctx.reply("§aVoce enviou §f" + sym() + amount + " §apara §f" + target.getName() + "§a.");
        target.send("§aVoce recebeu §f" + sym() + amount + " §ade §f" + sender.getName() + "§a.");
    }

    private int parseAmount(BukkitCommandContext ctx, int index) {
        try {
            return Integer.parseInt(ctx.getArgs()[index]);
        } catch (NumberFormatException e) {
            ctx.reply("§cValor invalido.");
            return -1;
        }
    }

    @Completer(name = "cash", subCommands = {"coins", "moedas"})
    public List<String> tabCash(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) return TabCompleteUtil.onlinePlayers(ctx.getArgs()[0]);
        return List.of();
    }

    @Completer(name = "cashgive", subCommands = {"givecash", "addcash"})
    public List<String> tabCashGive(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) return TabCompleteUtil.onlinePlayers(ctx.getArgs()[0]);
        if (ctx.getArgs().length == 2) return TabCompleteUtil.filter(ctx.getArgs()[1], "100", "500", "1000", "5000");
        return List.of();
    }

    @Completer(name = "cashtake", subCommands = {"takecash", "removecash"})
    public List<String> tabCashTake(BukkitCommandContext ctx) {
        return tabCashGive(ctx);
    }

    @Completer(name = "cashpay", subCommands = {"paycash", "enviarcash"})
    public List<String> tabCashPay(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) return TabCompleteUtil.onlinePlayersExceptSender(ctx);
        if (ctx.getArgs().length == 2) return TabCompleteUtil.filter(ctx.getArgs()[1], "100", "500", "1000");
        return List.of();
    }
}
