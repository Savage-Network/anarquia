package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.backend.data.ReportData;
import com.minecraft.lobbyhost.backend.model.PlayerReport;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.menu.report.ReportMenus;
import com.minecraft.lobbyhost.report.ReportStatus;
import com.minecraft.lobbyhost.util.ModernText;
import com.minecraft.lobbyhost.util.TabCompleteUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public final class ReportsCommand implements CommandInheritor {

    @Command(name = "reports", aliases = {"denuncias", "reportlist"}, playerOnly = true,
            permission = "lobbyhost.reports",
            description = "Gerencia denuncias (staff)",
            usage = "/reports [list|info|close|tp] [id] [nota]")
    public void execute(BukkitCommandContext ctx) {
        Account staff = ctx.account().orElse(null);
        if (staff == null || !staff.isStaffer()) {
            ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR + ">Sem permissão."));
            return;
        }

        ReportData data = Core.getReportData();
        if (data == null) {
            ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR + ">Reports indisponível."));
            return;
        }

        String[] args = ctx.getArgs();
        if (args.length == 0) {
            ReportMenus.openStaffList(ctx.player());
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "list", "lista", "abertas" -> listOpen(ctx, data, parseLimit(args, 1, 10));
            case "info", "ver", "show" -> {
                if (args.length < 2) {
                    ctx.reply(ModernText.parse(ModernText.hint("reports info <id>")));
                    return;
                }
                showInfo(ctx, data, args[1]);
            }
            case "close", "fechar", "resolver" -> {
                if (args.length < 2) {
                    ctx.reply(ModernText.parse(ModernText.hint("reports close <id> [nota]")));
                    return;
                }
                String note = args.length > 2 ? joinFrom(args, 2) : "Fechado";
                closeReport(ctx, staff, data, args[1], note, ReportStatus.CLOSED);
            }
            case "action", "punir" -> {
                if (args.length < 2) {
                    ctx.reply(ModernText.parse(ModernText.hint("reports action <id> [nota]")));
                    return;
                }
                String note = args.length > 2 ? joinFrom(args, 2) : "Ação tomada";
                closeReport(ctx, staff, data, args[1], note, ReportStatus.ACTIONED);
            }
            case "tp", "teleport", "ir" -> {
                if (args.length < 2) {
                    ctx.reply(ModernText.parse(ModernText.hint("reports tp <id>")));
                    return;
                }
                teleportToReporter(ctx, data, args[1]);
            }
            default -> listOpen(ctx, data, 8);
        }
    }

    private void listOpen(BukkitCommandContext ctx, ReportData data, int limit) {
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            List<PlayerReport> open = data.listOpen(limit);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                if (open.isEmpty()) {
                    ctx.reply(ModernText.parse(ModernText.mini("Nenhuma denúncia aberta.")));
                    return;
                }
                ctx.reply(ModernText.parse(ModernText.gradient("Denúncias abertas", ModernText.ERROR, "#FCA5A5")));
                for (PlayerReport r : open) {
                    ctx.reply(ModernText.parse("<!italic><#F1F5F9>#" + r.id() + " <#64748B>| "
                            + r.reporterName() + " → " + r.targetName()
                            + " <#64748B>| <" + ModernText.WARN + ">" + r.reason().label()
                            + " <#64748B>| " + r.serverId()));
                }
                ctx.reply(ModernText.parse(ModernText.hint("reports info <id>")));
            });
        });
    }

    private void showInfo(BukkitCommandContext ctx, ReportData data, String idRaw) {
        long id = parseId(idRaw);
        if (id < 0) {
            ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR + ">ID inválido."));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            Optional<PlayerReport> opt = data.findById(id);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> opt.ifPresentOrElse(r -> {
                ctx.reply(ModernText.parse(ModernText.gradient("Report #" + r.id(), ModernText.ERROR, "#FCA5A5")));
                ctx.reply(ModernText.parse(ModernText.mini("De: " + r.reporterName() + " → " + r.targetName())));
                ctx.reply(ModernText.parse(ModernText.mini("Motivo: " + r.reason().label() + " | Servidor: " + r.serverId())));
                ctx.reply(ModernText.parse(ModernText.mini("Status: " + r.status().name())));
                if (r.details() != null && !r.details().isBlank()) {
                    ctx.reply(ModernText.parse(ModernText.mini("Detalhes: " + r.details())));
                }
                if (r.staffNote() != null && !r.staffNote().isBlank()) {
                    ctx.reply(ModernText.parse(ModernText.mini("Staff: " + r.staffNote())));
                }
            }, () -> ctx.reply(ModernText.parse(ModernText.mini("Report não encontrado.")))));
        });
    }

    private void closeReport(BukkitCommandContext ctx, Account staff, ReportData data,
                             String idRaw, String note, ReportStatus status) {
        long id = parseId(idRaw);
        if (id < 0) {
            ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR + ">ID inválido."));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            boolean ok = data.close(id, staff.getId(), note, status);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                if (ok) {
                    ctx.reply(ModernText.parse("<!italic><" + ModernText.SUCCESS + ">Report #" + id
                            + " marcado como " + status.name() + "."));
                } else {
                    ctx.reply(ModernText.parse("<!italic><" + ModernText.WARN + ">Report não encontrado ou já fechado."));
                }
            });
        });
    }

    private void teleportToReporter(BukkitCommandContext ctx, ReportData data, String idRaw) {
        long id = parseId(idRaw);
        if (id < 0) {
            ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR + ">ID inválido."));
            return;
        }
        Player staffPlayer = ctx.player();
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            Optional<PlayerReport> opt = data.findById(id);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                if (opt.isEmpty()) {
                    ctx.reply(ModernText.parse(ModernText.mini("Report não encontrado.")));
                    return;
                }
                Player reporter = Bukkit.getPlayer(opt.get().reporterId());
                if (reporter == null || !reporter.isOnline()) {
                    ctx.reply(ModernText.parse(ModernText.mini("Denunciante offline.")));
                    return;
                }
                staffPlayer.teleport(reporter.getLocation());
                ctx.reply(ModernText.parse("<!italic><" + ModernText.SUCCESS + ">Teleportado até "
                        + reporter.getName() + "."));
            });
        });
    }

    private static long parseId(String raw) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private static int parseLimit(String[] args, int index, int def) {
        if (args.length <= index) return def;
        try {
            return Math.min(25, Math.max(1, Integer.parseInt(args[index])));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String joinFrom(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }

    @Completer(name = "reports", subCommands = {"denuncias", "reportlist"})
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.filter(ctx.getArgs()[0],
                    "list", "lista", "abertas", "info", "ver", "close", "fechar",
                    "action", "punir", "tp", "teleport", "ir");
        }
        return List.of();
    }
}
