package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.menu.ReportMenu;
import com.minecraft.lobbyhost.report.ReportReason;
import com.minecraft.lobbyhost.report.ReportService;
import com.minecraft.lobbyhost.util.ModernText;

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
            new ReportMenu(ctx.player(), account).open();
            return;
        }

        if (args.length == 1) {
            ReportReason pending = ReportService.getPendingReason(ctx.player());
            if (pending != null) {
                ReportService.submit(account, args[0], pending, "");
                return;
            }
            sendHelp(ctx);
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
            sendHelp(ctx);
            return;
        }

        ReportService.submit(account, args[0], reason, details);
    }

    private void sendHelp(BukkitCommandContext ctx) {
        ctx.reply(ModernText.parse(ModernText.mini("Uso: /report — abre o menu")));
        ctx.reply(ModernText.parse(ModernText.mini("Ou: /report <nick> <motivo> [detalhes]")));
        ctx.reply(ModernText.parse(ModernText.mini("Motivos: cheat, chat, assedio, bug, outro")));
    }
}
