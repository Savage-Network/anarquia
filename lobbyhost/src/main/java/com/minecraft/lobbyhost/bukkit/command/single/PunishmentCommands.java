package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.api.punishment.PunishmentCategory;
import com.minecraft.lobbyhost.api.punishment.PunishmentReason;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.menu.punish.PunishMenus;
import com.minecraft.lobbyhost.punishment.PunishmentPresets;
import com.minecraft.lobbyhost.punishment.PunishmentService;
import com.minecraft.lobbyhost.util.ModernText;
import com.minecraft.lobbyhost.util.PunishmentTimeUtil;
import com.minecraft.lobbyhost.util.TabCompleteUtil;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class PunishmentCommands implements CommandInheritor {

    @Command(name = "punir", aliases = {"punish", "p"}, rank = RankType.HELPER,
            permission = "lobbyhost.punish",
            description = "Pune um jogador (ban/mute)",
            usage = "/punir <jogador> | /punir <cat> <causa> <tempo> <nick> <motivo>")
    public void punir(BukkitCommandContext ctx) {
        if (!staffOk(ctx)) return;
        String[] args = ctx.getArgs();

        if (args.length == 1 && ctx.isPlayer()) {
            PunishMenus.open(ctx.player(), args[0]);
            return;
        }

        if (args.length == 2) {
            PunishmentCategory cat = PunishmentCategory.of(args[0]);
            if (cat != null) {
                showPresets(ctx, cat, args[1]);
                return;
            }
        }

        if (args.length < 4) {
            ctx.reply(ModernText.parse(ModernText.hint(
                    "punir <nick> — abre menu | punir BAN CHEATING -1 <nick> <motivo>")));
            return;
        }

        PunishmentCategory category = PunishmentCategory.of(args[0]);
        PunishmentReason reason = PunishmentReason.of(args[1]);
        if (category == null || reason == null) {
            ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR + ">Categoria ou causa inválida."));
            return;
        }

        boolean validTime = PunishmentTimeUtil.isValidTime(args[2])
                || args[2].equalsIgnoreCase("-1") || args[2].equalsIgnoreCase("perm");
        String target = args[validTime ? 3 : 2];
        String cause = ctx.joinArgs(validTime ? 4 : 3);
        if (cause.isBlank()) cause = "Não informado";

        runApply(ctx, target, category, reason, validTime ? args[2] : "-1", cause);
    }

    @Command(name = "despunir", aliases = {"unpunish", "unban", "unmute"}, rank = RankType.MOD,
            permission = "lobbyhost.unpunish",
            description = "Revoga punição por ID",
            usage = "/despunir <ID> <motivo>")
    public void despunir(BukkitCommandContext ctx) {
        if (!staffOk(ctx)) return;
        String[] args = ctx.getArgs();
        if (args.length < 2) {
            ctx.reply(ModernText.parse(ModernText.hint("despunir <ID> <motivo>")));
            return;
        }
        String id = args[0];
        if (!id.startsWith("#")) id = "#" + id;
        String reason = ctx.joinArgs(1);

        UUID authorId = null;
        String authorName = "CONSOLE";
        if (ctx.isPlayer()) {
            authorId = ctx.player().getUniqueId();
            authorName = ctx.player().getName();
        }

        String finalId = id;
        UUID finalAuthorId = authorId;
        String finalAuthorName = authorName;
        PunishmentService.revoke(finalAuthorId, finalAuthorName, finalId, reason)
                .thenAccept(r -> ctx.reply(ModernText.parse(r.success()
                        ? "<!italic><" + ModernText.SUCCESS + ">Punição revogada: <#F1F5F9>" + finalId
                        : "<!italic><" + ModernText.ERROR + ">" + r.message())));
    }

    @Command(name = "cban", rank = RankType.MOD, permission = "lobbyhost.cban",
            description = "Ban por cheating",
            usage = "/cban <nick> <tempo>")
    public void cban(BukkitCommandContext ctx) {
        if (!staffOk(ctx)) return;
        String[] args = ctx.getArgs();
        if (args.length < 2) {
            ctx.reply(ModernText.parse(ModernText.hint("cban <nick> <tempo>")));
            return;
        }
        runApply(ctx, args[0], PunishmentCategory.BAN, PunishmentReason.CHEATING, args[1], "Cheating");
    }

    @Command(name = "ban", rank = RankType.MOD, permission = "lobbyhost.ban",
            description = "Ban por regras da comunidade",
            usage = "/ban <nick> [tempo] [motivo]")
    public void ban(BukkitCommandContext ctx) {
        if (!staffOk(ctx)) return;
        String[] args = ctx.getArgs();
        if (args.length < 1) {
            ctx.reply(ModernText.parse(ModernText.hint("ban <nick> [tempo] [motivo]")));
            return;
        }
        BanArgs parsed = parseBanArgs(ctx, args);
        runApply(ctx, parsed.target(), PunishmentCategory.BAN, PunishmentReason.COMMUNITY,
                parsed.time(), parsed.cause());
    }

    @Command(name = "kick", rank = RankType.HELPER, permission = "lobbyhost.kick",
            description = "Expulsa jogador online (sem ban)",
            usage = "/kick <nick> [motivo]")
    public void kick(BukkitCommandContext ctx) {
        if (!staffOk(ctx)) return;
        String[] args = ctx.getArgs();
        if (args.length < 1) {
            ctx.reply(ModernText.parse(ModernText.hint("kick <nick> [motivo]")));
            return;
        }
        UUID authorId = null;
        String authorName = "CONSOLE";
        if (ctx.isPlayer()) {
            authorId = ctx.player().getUniqueId();
            authorName = ctx.player().getName();
        }
        String target = args[0];
        String reason = ctx.joinArgs(1);
        UUID finalAuthorId = authorId;
        String finalAuthorName = authorName;
        PunishmentService.kick(finalAuthorId, finalAuthorName, target, reason)
                .thenAccept(r -> ctx.reply(ModernText.parse(r.success()
                        ? "<!italic><" + ModernText.SUCCESS + "><#F1F5F9>" + r.targetName()
                        + " <" + ModernText.SUCCESS + ">expulso."
                        : "<!italic><" + ModernText.ERROR + ">" + r.message())));
    }

    @Command(name = "history", aliases = {"punishments", "punicoes"}, rank = RankType.HELPER,
            permission = "lobbyhost.punish",
            description = "Histórico de punições",
            usage = "/history <nick>")
    public void history(BukkitCommandContext ctx) {
        if (!staffOk(ctx)) return;
        if (ctx.getArgs().length < 1) {
            ctx.reply(ModernText.parse(ModernText.hint("history <nick>")));
            return;
        }
        if (ctx.isPlayer()) {
            PunishMenus.openHistory(ctx.player(), ctx.getArgs()[0]);
            return;
        }
        PunishmentService.resolveTarget(ctx.getArgs()[0]).thenAccept(opt -> {
            if (opt.isEmpty()) {
                ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR + ">Jogador não encontrado."));
                return;
            }
            var data = Core.getPunishmentData();
            if (data == null) return;
            var list = data.listByPlayer(opt.get().getId(), 8);
            ctx.reply(ModernText.parse("<!italic><" + ModernText.WARN + ">Punições de <#F1F5F9>"
                    + opt.get().getName() + "<" + ModernText.WARN + ">:"));
            if (list.isEmpty()) {
                ctx.reply(ModernText.parse("<!italic><" + ModernText.MUTED + ">Nenhuma punição registrada."));
                return;
            }
            for (var p : list) {
                String status = p.isActive() ? "ativa" : "revogada";
                ctx.reply(ModernText.parse("<!italic><#64748B>" + p.getId() + " <#F1F5F9>"
                        + p.getCategory().getDisplayName() + " <#64748B>| " + status
                        + " <#64748B>| " + p.getCause()));
            }
        });
    }

    @Command(name = "check", aliases = {"punishcheck", "verpunicao"}, rank = RankType.HELPER,
            permission = "lobbyhost.punish",
            description = "Consulta punição por ID ou jogador",
            usage = "/check <ID|nick>")
    public void check(BukkitCommandContext ctx) {
        if (!staffOk(ctx)) return;
        String[] args = ctx.getArgs();
        if (args.length < 1) {
            ctx.reply(ModernText.parse(ModernText.hint("check <ID|nick>")));
            return;
        }
        String query = args[0];
        var data = Core.getPunishmentData();
        if (data == null) {
            ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR + ">Sistema indisponível."));
            return;
        }

        if (query.startsWith("#") || query.matches("[a-zA-Z]{4}\\d")) {
            String id = query.startsWith("#") ? query : "#" + query;
            data.findById(id).ifPresentOrElse(p -> ctx.reply(formatPunishmentLine(p)),
                    () -> ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR
                            + ">Punição não encontrada.")));
            return;
        }

        PunishmentService.resolveTarget(query).thenAccept(opt -> {
            if (opt.isEmpty()) {
                ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR + ">Jogador não encontrado."));
                return;
            }
            Account target = opt.get();
            var activeBan = data.findActive(target.getId(), PunishmentCategory.BAN);
            var activeMute = data.findActive(target.getId(), PunishmentCategory.MUTE);
            ctx.reply(ModernText.parse("<!italic><" + ModernText.WARN + ">Consulta: <#F1F5F9>"
                    + target.getName()));
            if (activeBan.isPresent()) {
                ctx.reply(formatPunishmentLine(activeBan.get()));
            } else {
                ctx.reply(ModernText.parse("<!italic><" + ModernText.MUTED + ">Ban: nenhum ativo."));
            }
            if (activeMute.isPresent()) {
                ctx.reply(formatPunishmentLine(activeMute.get()));
            } else {
                ctx.reply(ModernText.parse("<!italic><" + ModernText.MUTED + ">Mute: nenhum ativo."));
            }
            var recent = data.listByPlayer(target.getId(), 5);
            if (!recent.isEmpty()) {
                ctx.reply(ModernText.parse("<!italic><#64748B>Últimas punições:"));
                for (var p : recent) {
                    ctx.reply(formatPunishmentLine(p));
                }
            }
        });
    }

    private net.kyori.adventure.text.Component formatPunishmentLine(
            com.minecraft.lobbyhost.api.punishment.Punishment p) {
        String status = p.isActive() ? "ativa" : "revogada";
        String duration = p.isTemporary()
                ? PunishmentTimeUtil.formatRemaining(p.getExpiresAt())
                : "Permanente";
        return ModernText.parse("<!italic><#F1F5F9>" + p.getId()
                + " <#64748B>| <#F1F5F9>" + p.getCategory().getDisplayName()
                + " <#64748B>| " + status
                + " <#64748B>| " + duration
                + " <#64748B>| " + p.getCause());
    }

    private void showPresets(BukkitCommandContext ctx, PunishmentCategory cat, String target) {
        ctx.reply(ModernText.parse("<!italic><" + ModernText.WARN + ">" + cat.getDisplayName()
                + " <#64748B>para <#F1F5F9>" + target));
        for (var p : PunishmentPresets.forCategory(cat)) {
            String cmd = "/punir " + p.category().name() + " " + p.reason().name()
                    + " " + p.time() + " " + target + " " + p.label();
            ctx.reply(ModernText.parse("<!italic><#F1F5F9>" + p.label()
                            + " <#64748B>(" + PunishmentPresets.formatTime(p.time()) + ")")
                    .clickEvent(ClickEvent.suggestCommand(cmd))
                    .hoverEvent(HoverEvent.showText(ModernText.parse(ModernText.mini("Clique para sugerir")))));
        }
    }

    private record BanArgs(String target, String time, String cause) {}

    private static BanArgs parseBanArgs(BukkitCommandContext ctx, String[] args) {
        String target = args[0];
        String time = "-1";
        String cause = "Não informado";
        if (args.length >= 2) {
            if (PunishmentTimeUtil.isValidTime(args[1])
                    || args[1].equalsIgnoreCase("-1")
                    || args[1].equalsIgnoreCase("perm")
                    || args[1].equalsIgnoreCase("perma")) {
                time = args[1];
                if (args.length >= 3) cause = ctx.joinArgs(2);
            } else {
                cause = ctx.joinArgs(1);
            }
        }
        return new BanArgs(target, time, cause);
    }

    private void runApply(BukkitCommandContext ctx, String target, PunishmentCategory category,
                          PunishmentReason reason, String time, String cause) {
        UUID authorId = null;
        String authorName = "CONSOLE";
        if (ctx.isPlayer()) {
            authorId = ctx.player().getUniqueId();
            authorName = ctx.player().getName();
        }
        UUID finalAuthorId = authorId;
        String finalAuthorName = authorName;
        PunishmentService.apply(finalAuthorId, finalAuthorName, target, category, reason, time, cause)
                .thenAccept(r -> ctx.reply(ModernText.parse(r.success()
                        ? "<!italic><" + ModernText.SUCCESS + ">Punição aplicada em <#F1F5F9>" + r.targetName()
                        + " <#64748B>ID <#F1F5F9>" + r.punishment().getId()
                        : "<!italic><" + ModernText.ERROR + ">" + r.message())));
    }

    private boolean staffOk(BukkitCommandContext ctx) {
        Account acc = ctx.account().orElse(null);
        if (acc != null && acc.isStaffer()) return true;
        if (ctx.getSender().hasPermission("lobbyhost.punish")) return true;
        ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR + ">Sem permissão."));
        return false;
    }

    @Completer(name = "punir", subCommands = {"punish", "p"})
    public List<String> tabPunir(BukkitCommandContext ctx) {
        String[] args = ctx.getArgs();
        if (args.length == 1) {
            List<String> out = new ArrayList<>(TabCompleteUtil.onlinePlayersExceptSender(ctx));
            out.addAll(TabCompleteUtil.filter(args[0], TabCompleteUtil.punishmentCategories()));
            return out;
        }
        if (args.length == 2 && PunishmentCategory.of(args[0]) != null) {
            return TabCompleteUtil.filter(args[1], TabCompleteUtil.punishmentReasons());
        }
        if (args.length == 3 && PunishmentCategory.of(args[0]) != null) {
            return TabCompleteUtil.filter(args[2], TabCompleteUtil.punishmentTimes());
        }
        if (args.length == 2) {
            return TabCompleteUtil.onlinePlayers(args[1]);
        }
        return List.of();
    }

    @Completer(name = "despunir", subCommands = {"unpunish", "unban", "unmute"})
    public List<String> tabDespunir(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            String partial = ctx.getArgs()[0];
            if (partial.startsWith("#") || partial.isEmpty()) {
                return TabCompleteUtil.filter(partial, "#");
            }
        }
        return List.of();
    }

    @Completer(name = "cban")
    public List<String> tabCban(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) return TabCompleteUtil.onlinePlayersExceptSender(ctx);
        if (ctx.getArgs().length == 2) return TabCompleteUtil.filter(ctx.getArgs()[1], TabCompleteUtil.punishmentTimes());
        return List.of();
    }

    @Completer(name = "ban")
    public List<String> tabBan(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) return TabCompleteUtil.onlinePlayersExceptSender(ctx);
        if (ctx.getArgs().length == 2) {
            return TabCompleteUtil.filter(ctx.getArgs()[1], TabCompleteUtil.punishmentTimes());
        }
        return List.of();
    }

    @Completer(name = "kick")
    public List<String> tabKick(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) return TabCompleteUtil.onlinePlayersExceptSender(ctx);
        return List.of();
    }

    @Completer(name = "history", subCommands = {"punishments", "punicoes"})
    public List<String> tabHistory(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) return TabCompleteUtil.onlinePlayers(ctx.getArgs()[0]);
        return List.of();
    }

    @Completer(name = "check", subCommands = {"punishcheck", "verpunicao"})
    public List<String> tabCheck(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) return TabCompleteUtil.onlinePlayers(ctx.getArgs()[0]);
        return List.of();
    }
}
