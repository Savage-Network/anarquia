package com.minecraft.proxyhost.velocity.command;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
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
            case "ban" -> ban(src, args);
            case "kick" -> kick(src, args);
            default -> punir(src, args);
        }
    }

    private void punir(CommandSource src, String[] args) {
        if (!hasRankPermission(src, "proxyhost.punish", RankType.HELPER)) {
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
            src.sendMessage(legacy("§c§lNenhum preset disponível para " + cat.getDisplayName()));
            return;
        }
        src.sendMessage(legacy("\n§6§l⚡ PRESETS DE PUNIÇÃO ⚡"));
        src.sendMessage(legacy("§7Categoria: §e" + cat.getDisplayName() + " §7| Alvo: §a" + target));
        src.sendMessage(legacy(""));
        int i = 1;
        for (Preset p : presets) {
            String time = p.time().equals("-1") ? "§4Permanente" : "§e" + p.time();
            src.sendMessage(legacy("§8[§6" + i++ + "§8] §f" + p.label() + " §8(§7Duração: " + time + "§8)"));
            src.sendMessage(legacy("§8    └─ §7/punir " + p.category().name() + " " + p.reason().name()
                    + " " + p.time() + " " + target + " " + p.label()));
        }
        src.sendMessage(legacy(""));
    }

    private void cban(CommandSource src, String[] args) {
        if (!hasRankPermission(src, "proxyhost.cban", RankType.MOD)) {
            src.sendMessage(legacy("&cSem permissão."));
            return;
        }
        if (args.length < 2) {
            src.sendMessage(legacy("&cUso: /cban <nick> <tempo>"));
            return;
        }
        runApply(src, args[0], PunishmentCategory.BAN, PunishmentReason.CHEATING, args[1], "Cheating");
    }

    private void ban(CommandSource src, String[] args) {
        if (!hasRankPermission(src, "proxyhost.ban", RankType.MOD)) {
            src.sendMessage(legacy("&cSem permissão."));
            return;
        }
        if (args.length < 1) {
            src.sendMessage(legacy("&cUso: /ban <nick> [tempo] [motivo]"));
            return;
        }
        BanArgs parsed = parseBanArgs(args);
        runApply(src, parsed.target(), PunishmentCategory.BAN, PunishmentReason.COMMUNITY,
                parsed.time(), parsed.cause());
    }

    private void kick(CommandSource src, String[] args) {
        if (!hasRankPermission(src, "proxyhost.kick", RankType.HELPER)) {
            src.sendMessage(legacy("&cSem permissão."));
            return;
        }
        if (args.length < 1) {
            src.sendMessage(legacy("&cUso: /kick <nick> [motivo]"));
            return;
        }
        UUID authorId = null;
        String authorName = "CONSOLE";
        if (src instanceof Player p) {
            authorId = p.getUniqueId();
            authorName = p.getUsername();
        }
        String target = args[0];
        String reason = join(args, 1);
        UUID finalAuthorId = authorId;
        String finalAuthorName = authorName;
        PunishmentService.kick(finalAuthorId, finalAuthorName, target, reason)
                .thenAccept(result -> {
                    if (result.success()) {
                        src.sendMessage(legacy("&a" + result.targetName() + " expulso da rede."));
                    } else {
                        src.sendMessage(legacy("&c" + result.message()));
                    }
                });
    }

    private record BanArgs(String target, String time, String cause) {}

    private static BanArgs parseBanArgs(String[] args) {
        String target = args[0];
        String time = "-1";
        String cause = "Não informado";
        if (args.length >= 2) {
            if (com.minecraft.proxyhost.util.PunishmentTimeUtil.isValidTime(args[1])
                    || args[1].equalsIgnoreCase("-1")
                    || args[1].equalsIgnoreCase("perm")
                    || args[1].equalsIgnoreCase("perma")) {
                time = args[1];
                if (args.length >= 3) cause = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            } else {
                cause = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return new BanArgs(target, time, cause);
    }

    private void despunir(CommandSource src, String[] args) {
        if (!hasRankPermission(src, "proxyhost.unpunish", RankType.MOD)) {
            src.sendMessage(legacy("§c§lSem permissão."));
            return;
        }
        if (args.length < 2) {
            src.sendMessage(legacy("§e§lUso: §7/despunir <ID> <motivo>"));
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
                .thenAccept(result -> {
                    if (result.success()) {
                        src.sendMessage(legacy("\n§a§l✓ PUNIÇÃO REVOGADA!"));
                        src.sendMessage(legacy("§7ID: §6" + finalId));
                        src.sendMessage(legacy("§7Jogador: §f" + result.targetName()));
                        src.sendMessage(legacy("§7Motivo: §e" + reason));
                        src.sendMessage(legacy(""));
                    } else {
                        src.sendMessage(legacy("§c§l✘ ERRO: §c" + result.message()));
                    }
                });
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
                        src.sendMessage(legacy("§c§l✘ ERRO: §c" + result.message()));
                        return;
                    }
                    src.sendMessage(legacy("\n§a§l✓ PUNIÇÃO APLICADA COM SUCESSO!"));
                    src.sendMessage(legacy("§7Jogador: §f" + result.targetName()));
                    src.sendMessage(legacy("§7Tipo: §e" + category.getDisplayName()));
                    src.sendMessage(legacy("§7Motivo: §c" + reason.getDisplayName()));
                    src.sendMessage(legacy("§7Detalhes: §f" + cause));
                    src.sendMessage(legacy("§7ID: §6" + result.punishment().getId()));
                    src.sendMessage(legacy(""));
                });
    }

    private static String join(String[] args, int from) {
        if (args.length <= from) return "Não informado";
        return String.join(" ", Arrays.copyOfRange(args, from, args.length));
    }

    private static net.kyori.adventure.text.Component legacy(String s) {
        return LEGACY.deserialize(s.replace('&', '§'));
    }

    private static boolean hasRankPermission(CommandSource src, String node, RankType minRank) {
        if (src instanceof Player player) {
            Account account = Core.getAccountController().of(player.getUniqueId());
            if (account != null) {
                if (account.hasPermission(node)) return true;
                if (account.hasRank(minRank)) return true;
            }
        }
        return src.hasPermission(node);
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
