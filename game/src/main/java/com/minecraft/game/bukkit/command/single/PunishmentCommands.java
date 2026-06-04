package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.api.punishment.PunishmentCategory;
import com.minecraft.game.api.punishment.PunishmentReason;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.punishment.PunishmentService;
import com.minecraft.game.util.ModernText;
import com.minecraft.game.util.PunishmentTimeUtil;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PunishmentCommands implements CommandInheritor {

    private record Preset(PunishmentCategory category, PunishmentReason reason, String time, String label) {}

    private static final Map<PunishmentCategory, List<Preset>> PRESETS = new LinkedHashMap<>();

    static {
        PRESETS.put(PunishmentCategory.BAN, List.of(
                new Preset(PunishmentCategory.BAN, PunishmentReason.CHEATING, "-1", "Uso de Trapaças"),
                new Preset(PunishmentCategory.BAN, PunishmentReason.COMMUNITY, "7d", "Assédio sexual"),
                new Preset(PunishmentCategory.BAN, PunishmentReason.COMMUNITY, "14d", "Discriminação"),
                new Preset(PunishmentCategory.BAN, PunishmentReason.COMMUNITY, "14d", "Skin inapropriada")
        ));
        PRESETS.put(PunishmentCategory.MUTE, List.of(
                new Preset(PunishmentCategory.MUTE, PunishmentReason.COMMUNITY, "1h", "Spam/Flood"),
                new Preset(PunishmentCategory.MUTE, PunishmentReason.COMMUNITY, "6h", "Linguagem inadequada")
        ));
    }

    @Command(name = "punir", aliases = {"punish", "p"}, rank = RankType.HELPER,
            permission = "game.punish",
            description = "Pune um jogador (ban/mute)",
            usage = "/punir <jogador> | /punir <cat> <causa> <tempo> <nick> <motivo>")
    public void punir(BukkitCommandContext ctx) {
        if (!staffOk(ctx)) return;
        String[] args = ctx.getArgs();

        if (args.length == 2) {
            PunishmentCategory cat = PunishmentCategory.of(args[0]);
            if (cat != null) {
                showPresets(ctx, cat, args[1]);
                return;
            }
        }

        if (args.length < 4) {
            ctx.reply(ModernText.parse(ModernText.hint(
                    "punir <nick> ou punir BAN CHEATING -1 <nick> <motivo>")));
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
            permission = "game.unpunish",
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

    @Command(name = "cban", rank = RankType.MOD, permission = "game.cban",
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

    @Command(name = "history", aliases = {"punishments", "punicoes"}, rank = RankType.HELPER,
            permission = "game.punish",
            description = "Histórico de punições",
            usage = "/history <nick>")
    public void history(BukkitCommandContext ctx) {
        if (!staffOk(ctx)) return;
        if (ctx.getArgs().length < 1) {
            ctx.reply(ModernText.parse(ModernText.hint("history <nick>")));
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

    private void showPresets(BukkitCommandContext ctx, PunishmentCategory cat, String target) {
        List<Preset> presets = PRESETS.get(cat);
        if (presets == null) return;
        ctx.reply(ModernText.parse("<!italic><" + ModernText.WARN + ">" + cat.getDisplayName()
                + " <#64748B>para <#F1F5F9>" + target));
        for (Preset p : presets) {
            String cmd = "/punir " + p.category().name() + " " + p.reason().name()
                    + " " + p.time() + " " + target + " " + p.label();
            ctx.reply(ModernText.parse("<!italic><#F1F5F9>" + p.label()
                            + " <#64748B>(" + (p.time().equals("-1") ? "permanente" : p.time()) + ")")
                    .clickEvent(ClickEvent.suggestCommand(cmd))
                    .hoverEvent(HoverEvent.showText(ModernText.parse(ModernText.mini("Clique para sugerir")))));
        }
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
        if (ctx.getSender().hasPermission("game.punish")) return true;
        ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR + ">Sem permissão."));
        return false;
    }
}
