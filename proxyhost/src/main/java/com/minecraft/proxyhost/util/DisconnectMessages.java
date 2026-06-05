package com.minecraft.proxyhost.util;

import com.minecraft.proxyhost.ProxySettings;
import com.minecraft.proxyhost.api.punishment.Punishment;
import com.minecraft.proxyhost.api.punishment.PunishmentCategory;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Telas de desconexao/kick do proxy SavageMC (manutencao, ban, servidor offline, etc.).
 */
public final class DisconnectMessages {

    private DisconnectMessages() {}

    public static final String BRAND = "§6§lSavageMC";
    public static final String SEPARATOR = "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";

    public static Component maintenance(List<String> customLines) {
        if (customLines != null && !customLines.isEmpty()) {
            return LegacyTexts.joinLines(LegacyTexts.colorize(customLines));
        }
        return screen(
                header(),
                " ",
                "§c§lMANUTENÇÃO",
                "§7O servidor está em manutenção no momento.",
                "§7Apenas membros autorizados da equipe podem entrar.",
                " ",
                "§7Acompanhe novidades em §6" + ProxySettings.SERVER_ADDRESS,
                footer()
        );
    }

    public static Component accountLoadFailed() {
        return screen(
                header(),
                " ",
                "§c§lERRO AO ENTRAR",
                "§7Não foi possível carregar sua conta.",
                "§7Tente novamente em alguns instantes.",
                " ",
                "§7Persistindo? Contate o suporte em §6" + ProxySettings.SERVER_ADDRESS,
                footer()
        );
    }

    public static Component ban(Punishment punishment) {
        List<String> lines = new ArrayList<>();
        lines.add(header());
        lines.add(" ");
        lines.add("§c§l✘ CONTA SUSPENSA ✘");
        lines.add(" ");

        if (punishment.getCategory() == PunishmentCategory.BAN) {
            lines.add(punishment.isTemporary()
                    ? "§7Você foi §ebanido temporariamente §7da rede."
                    : "§7Você foi §4banido permanentemente §7da rede.");
        } else {
            lines.add("§7Sua conta está §crestrita §7no momento.");
        }
        lines.add(" ");
        lines.add("§7Motivo: §c" + punishment.getReason().getDisplayName());
        if (punishment.getCause() != null && !punishment.getCause().isBlank()) {
            lines.add("§7Detalhes: §f" + punishment.getCause());
        }
        if (punishment.isTemporary()) {
            lines.add("§7Expira em: §e" + PunishmentTimeUtil.formatRemaining(punishment.getExpiresAt()));
        }
        lines.add(" ");
        lines.add("§7ID: §8" + punishment.getId());
        lines.add("§7Apelo: §6" + ProxySettings.SERVER_STORE_URL);
        lines.add(footer());
        return screen(lines);
    }

    public static String muteNotice(Punishment punishment) {
        StringBuilder msg = new StringBuilder();
        msg.append("\n").append(SEPARATOR).append("\n");
        msg.append(BRAND).append("\n\n");
        msg.append("§c§l⚠ VOCÊ FOI MUTADO\n\n");
        msg.append(punishment.isTemporary()
                ? "§7Você foi §emutado temporariamente§7.\n"
                : "§7Você foi §4mutado permanentemente§7.\n\n");
        msg.append("§7Motivo: §c").append(punishment.getReason().getDisplayName()).append("\n");
        if (punishment.getCause() != null && !punishment.getCause().isBlank()) {
            msg.append("§7Detalhes: §f").append(punishment.getCause()).append("\n");
        }
        if (punishment.isTemporary()) {
            msg.append("§7Expira em: §e")
                    .append(PunishmentTimeUtil.formatRemaining(punishment.getExpiresAt()))
                    .append("\n");
        }
        msg.append("\n§7ID: §8").append(punishment.getId()).append("\n");
        msg.append(SEPARATOR);
        return msg.toString();
    }

    public static Component staffKick(String staffName, String reason) {
        if (reason == null || reason.isBlank()) reason = "Não informado";
        if (staffName == null || staffName.isBlank()) staffName = "CONSOLE";
        return screen(
                header(),
                " ",
                "§c§lEXPULSO",
                "§7Você foi expulso da rede.",
                " ",
                "§7Motivo: §f" + trim(reason, 120),
                "§7Staff: §e" + staffName,
                footer()
        );
    }

    public static Component serverClosed(String serverName) {
        String display = formatServerName(serverName);
        return screen(
                header(),
                " ",
                "§e§lSERVIDOR INDISPONÍVEL",
                "§7O modo §f" + display + " §7está offline ou reiniciando.",
                "§7Você foi desconectado da rede por segurança.",
                " ",
                "§7Aguarde alguns instantes e entre novamente.",
                "§7Site: §6" + ProxySettings.SERVER_ADDRESS,
                footer()
        );
    }

    public static Component serverKick(String serverName, String reason) {
        String display = formatServerName(serverName);
        List<String> lines = new ArrayList<>();
        lines.add(header());
        lines.add(" ");
        lines.add("§c§lDESCONECTADO");
        lines.add("§7Servidor: §f" + display);
        if (reason != null && !reason.isBlank() && !isGenericReason(reason)) {
            lines.add("§7Motivo: §f" + trim(reason, 120));
        }
        lines.add(" ");
        lines.add("§7Volte ao lobby e tente novamente.");
        lines.add(footer());
        return screen(lines);
    }

    public static Component proxyShutdown() {
        return screen(
                header(),
                " ",
                "§e§lREDE REINICIANDO",
                "§7O proxy SavageMC está sendo reiniciado.",
                "§7Entre novamente em alguns instantes.",
                " ",
                "§7Obrigado por jogar conosco!",
                footer()
        );
    }

    public static Component redirectNotice(String targetServer) {
        return screen(
                header(),
                " ",
                "§e§lMODO INDISPONÍVEL",
                "§7§f" + formatServerName(targetServer) + " §7está reiniciando.",
                "§7Você foi enviado ao lobby automaticamente.",
                footer()
        );
    }

    /** Nao sobrescrever kicks intencionais (ban, staff, plugins). */
    public static boolean isProtectedKick(String plain) {
        if (plain == null || plain.isBlank()) return false;
        String low = plain.toLowerCase(Locale.ROOT);
        return low.contains("banido")
                || low.contains("ban ")
                || low.contains("suspens")
                || low.contains("punido")
                || low.contains("você foi banido")
                || low.contains("voce foi banido")
                || low.contains("conta suspensa")
                || low.contains("manutenção")
                || low.contains("manutencao")
                || low.contains("expulso")
                || low.contains("expuls")
                || low.contains("erro ao entrar")
                || low.contains("erro ao tentar carregar");
    }

    public static boolean isGenericReason(String plain) {
        if (plain == null || plain.isBlank()) return true;
        String low = plain.toLowerCase(Locale.ROOT);
        return low.contains("disconnected")
                || low.contains("connection")
                || low.contains("internal")
                || low.contains("timed out")
                || low.contains("server closed")
                || low.contains("shutting down")
                || low.contains("restarting")
                || low.contains("ioexception")
                || low.contains("unable to connect")
                || low.contains("connection reset")
                || low.equals("null")
                || low.length() < 4;
    }

    private static String header() {
        return BRAND + "\n§7" + ProxySettings.SERVER_ADDRESS;
    }

    private static String footer() {
        return SEPARATOR;
    }

    private static Component screen(String... lines) {
        return screen(List.of(lines));
    }

    private static Component screen(List<String> lines) {
        return LegacyTexts.joinLines(LegacyTexts.colorize(lines));
    }

    private static String formatServerName(String server) {
        if (server == null || server.isBlank()) return "Rede";
        return server.substring(0, 1).toUpperCase(Locale.ROOT) + server.substring(1).toLowerCase(Locale.ROOT);
    }

    private static String trim(String s, int max) {
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }
}
