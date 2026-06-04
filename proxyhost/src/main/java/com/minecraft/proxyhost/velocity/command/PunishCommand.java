package com.minecraft.proxyhost.velocity.command;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.context.objects.rank.RankType;
import com.minecraft.proxyhost.api.punishment.PunishmentCategory;
import com.minecraft.proxyhost.api.punishment.PunishmentReason;
import com.minecraft.proxyhost.punishment.PunishmentService;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * /punir, /despunir e /cban no proxy (estilo Savage 1.8).
 */
public final class PunishCommand implements SimpleCommand {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

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

    @Override
    public void execute(Invocation invocation) {
        CommandSource src = invocation.source();
        String label = invocation.alias().toLowerCase(Locale.ROOT);
        String[] args = invocation.arguments();

        switch (label) {
            case "despunir", "unpunish", "unban", "unmute" -> despunir(src, args);
            case "cban" -> cban(src, args);
            default -> punir(src, args);
        }
    }

    private void punir(CommandSource src, String[] args) {
        if (!src.hasPermission("proxyhost.punish")) {
            src.sendMessage(legacy("&cSem permissão."));
            return;
        }

        if (args.length == 1) {
            src.sendMessage(legacy("&eUso: /punir <jogador> ou /punir <categoria> <causa> <tempo> <jogador> <motivo>"));
            src.sendMessage(legacy("&7Categorias: &fBAN &7| &fMUTE &7| Causas: &fCHEATING &7| &fCOMMUNITY"));
            return;
        }

        if (args.length == 2) {
            PunishmentCategory cat = PunishmentCategory.of(args[0]);
            if (cat != null) {
                showPresets(src, cat, args[1]);
                return;
            }
        }

        if (args.length < 4) {
            src.sendMessage(legacy("&cUso: /punir [categoria] [causa] [tempo] [jogador] [motivo]"));
            return;
        }

        PunishmentCategory category = PunishmentCategory.of(args[0]);
        PunishmentReason reason = PunishmentReason.of(args[1]);
        if (category == null || reason == null) {
            src.sendMessage(legacy("&cCategoria ou causa inválida."));
            return;
        }

        boolean validTime = com.minecraft.proxyhost.util.PunishmentTimeUtil.isValidTime(args[2])
                || args[2].equalsIgnoreCase("-1")
                || args[2].equalsIgnoreCase("perm");
        int targetIdx = validTime ? 3 : 2;
        String targetName = args[targetIdx];
        String cause = join(args, validTime ? 4 : 3);

        runApply(src, targetName, category, reason, validTime ? args[2] : "-1", cause);
    }

    private void showPresets(CommandSource src, PunishmentCategory cat, String target) {
        List<Preset> presets = PRESETS.get(cat);
        if (presets == null || presets.isEmpty()) {
            src.sendMessage(legacy("&cNenhum preset para " + cat.getDisplayName()));
            return;
        }
        src.sendMessage(legacy("&e" + cat.getDisplayName() + " &7para &a" + target + "&7:"));
        int i = 1;
        for (Preset p : presets) {
            String time = p.time().equals("-1") ? "Permanente" : p.time();
            src.sendMessage(legacy("&7[" + i++ + "] &f" + p.label() + " &8(" + time + ")"));
            src.sendMessage(legacy("&8  /punir " + p.category().name() + " " + p.reason().name()
                    + " " + p.time() + " " + target + " " + p.label()));
        }
    }

    private void cban(CommandSource src, String[] args) {
        if (!src.hasPermission("proxyhost.cban")) {
            src.sendMessage(legacy("&cSem permissão."));
            return;
        }
        if (args.length < 2) {
            src.sendMessage(legacy("&cUso: /cban <nick> <tempo>"));
            return;
        }
        runApply(src, args[0], PunishmentCategory.BAN, PunishmentReason.CHEATING, args[1], "Cheating");
    }

    private void despunir(CommandSource src, String[] args) {
        if (!src.hasPermission("proxyhost.unpunish")) {
            src.sendMessage(legacy("&cSem permissão."));
            return;
        }
        if (args.length < 2) {
            src.sendMessage(legacy("&cUso: /despunir <ID> <motivo>"));
            return;
        }
        String punishmentId = args[0];
        if (!punishmentId.startsWith("#")) punishmentId = "#" + punishmentId;
        String reason = join(args, 1);

        UUID authorId = null;
        String authorName = "CONSOLE";
        if (src instanceof Player p) {
            authorId = p.getUniqueId();
            authorName = p.getUsername();
        }

        UUID finalAuthorId = authorId;
        String finalAuthorName = authorName;
        String finalId = punishmentId;
        PunishmentService.revoke(finalAuthorId, finalAuthorName, finalId, reason)
                .thenAccept(result -> src.sendMessage(legacy(result.success()
                        ? "&aPunição " + finalId + " revogada."
                        : "&c" + result.message())));
    }

    private void runApply(CommandSource src, String targetName, PunishmentCategory category,
                          PunishmentReason reason, String time, String cause) {
        UUID authorId = null;
        String authorName = "CONSOLE";
        if (src instanceof Player p) {
            authorId = p.getUniqueId();
            authorName = p.getUsername();
        }
        UUID finalAuthorId = authorId;
        String finalAuthorName = authorName;
        PunishmentService.apply(finalAuthorId, finalAuthorName, targetName, category, reason, time, cause)
                .thenAccept(result -> {
                    if (!result.success()) {
                        src.sendMessage(legacy("&c" + result.message()));
                        return;
                    }
                    src.sendMessage(legacy("&cVocê puniu &f" + result.targetName()
                            + " &7(" + category.getDisplayName() + "/" + reason.getDisplayName()
                            + ") &8ID: &f" + result.punishment().getId()));
                });
    }

    private static String join(String[] args, int from) {
        if (args.length <= from) return "Não informado";
        return String.join(" ", Arrays.copyOfRange(args, from, args.length));
    }

    private static net.kyori.adventure.text.Component legacy(String s) {
        return LEGACY.deserialize(s.replace('&', '§'));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            List<String> out = new ArrayList<>();
            out.addAll(Arrays.stream(PunishmentCategory.values()).map(Enum::name).toList());
            Core.getProxy().getAllPlayers().forEach(p -> out.add(p.getUsername()));
            return out;
        }
        return Collections.emptyList();
    }
}
