package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.menu.report.ReportMenus;
import com.minecraft.lobbyhost.report.ReportReason;
import com.minecraft.lobbyhost.report.ReportService;
import com.minecraft.lobbyhost.util.ModernText;
import com.minecraft.lobbyhost.util.TabCompleteUtil;

import java.util.ArrayList;
import java.util.List;

public final class ReportCommand implements CommandInheritor {

    @Command(name = "report", aliases = {"denunciar", "denuncia"}, playerOnly = true,
            permission = "lobbyhost.report",
            description = "Denuncia um jogador",
            usage = "/report [nick] [motivo] [detalhes...]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR + ">Conta não carregada."));
            return;
        }

        String[] args = ctx.getArgs();
        if (args.length == 0) {
            ReportMenus.open(ctx.player(), account);
            return;
        }

        if (args.length == 1) {
            ReportReason pending = ReportService.getPendingReason(ctx.player());
            if (pending != null) {
                ReportService.submit(account, args[0], pending, "");
                return;
            }
            ReportMenus.open(ctx.player(), account);
            return;
        }

        ReportReason reason = ReportReason.fromInput(args[1]);
        if (reason == null) {
            reason = ReportService.getPendingReason(ctx.player());
        }
        String details = "";
        if (args.length > 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                if (i > 2) sb.append(' ');
                sb.append(args[i]);
            }
            details = sb.toString();
        }

        if (reason == null) {
            ctx.reply(ModernText.parse(ModernText.mini("Motivo inválido. Use /report para abrir o menu.")));
            ctx.reply(ModernText.parse(ModernText.mini("Motivos: cheat, chat, assedio, bug, outro")));
            return;
        }

        ReportService.submit(account, args[0], reason, details);
    }

    @Completer(name = "report", subCommands = {"denunciar", "denuncia"})
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.onlinePlayersExceptSender(ctx);
        }
        if (ctx.getArgs().length == 2) {
            List<String> reasons = new ArrayList<>();
            for (ReportReason r : ReportReason.values()) reasons.add(r.name().toLowerCase());
            reasons.addAll(List.of("cheat", "chat", "assedio", "bug", "outro"));
            return TabCompleteUtil.filter(ctx.getArgs()[1], reasons);
        }
        return List.of();
    }
}
