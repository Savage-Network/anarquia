package com.minecraft.lobbyhost.report;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.backend.data.ReportData;
import com.minecraft.lobbyhost.backend.model.PlayerReport;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ReportService {

    public static final long REPORT_COOLDOWN_MS = 5 * 60_000L;
    public static final long DUPLICATE_WINDOW_HOURS = 12L;
    public static final int DETAILS_MAX = 280;

    private static final Map<UUID, ReportReason> PENDING_REASON = new ConcurrentHashMap<>();

    private ReportService() {}

    public static void setPendingReason(Player player, ReportReason reason) {
        if (player == null) return;
        ReportContext ctx = ReportContext.of(player.getUniqueId());
        if (ctx == null) ctx = ReportContext.open(player.getUniqueId());
        ctx.setReason(reason);
        PENDING_REASON.put(player.getUniqueId(), reason);
    }

    public static ReportReason getPendingReason(Player player) {
        if (player == null) return null;
        ReportContext ctx = ReportContext.of(player.getUniqueId());
        if (ctx != null && ctx.reason() != null) return ctx.reason();
        return PENDING_REASON.get(player.getUniqueId());
    }

    public static void clearPending(Player player) {
        if (player != null) {
            PENDING_REASON.remove(player.getUniqueId());
            ReportContext.close(player.getUniqueId());
        }
    }

    public static String serverId() {
        return LobbySettings.SERVER_MODE.toLowerCase();
    }

    public static void submit(Account reporter, String targetName, ReportReason reason, String details) {
        if (reporter == null || !reporter.isOnline()) {
            return;
        }
        if (reason == null) {
            reporter.send(ModernText.parse(ModernText.mini(
                    "Motivo inválido. Use: cheat, chat, assedio, bug, outro")));
            return;
        }

        Player reporterPlayer = reporter.player();
        if (reporter.hasCooldown(Constant.REPORT_SEND_COOLDOWN_KEY)) {
            long sec = Math.max(1, reporter.getCooldownRemaining(Constant.REPORT_SEND_COOLDOWN_KEY) / 1000L);
            reporter.send(ModernText.parse("<!italic><" + ModernText.WARN + ">Aguarde <#F1F5F9>" + sec
                    + "s <" + ModernText.WARN + ">para denunciar novamente."));
            return;
        }

        OfflinePlayer targetOff = Bukkit.getOfflinePlayer(targetName);
        if (!targetOff.hasPlayedBefore() && !targetOff.isOnline()) {
            reporter.send(ModernText.parse("<!italic><" + ModernText.ERROR + ">Jogador não encontrado."));
            return;
        }

        UUID targetId = targetOff.getUniqueId();
        if (targetId.equals(reporter.getId())) {
            reporter.send(ModernText.parse("<!italic><" + ModernText.ERROR + ">Você não pode se denunciar."));
            return;
        }

        String cleanDetails = sanitize(details);
        String displayTarget = targetOff.getName() != null ? targetOff.getName() : targetName;

        ReportData data = Core.getReportData();
        if (data == null) {
            reporter.send(ModernText.parse("<!italic><" + ModernText.ERROR + ">Sistema de reports indisponível."));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            if (data.hasRecentDuplicate(reporter.getId(), targetId, DUPLICATE_WINDOW_HOURS)) {
                runSync(reporterPlayer, () -> reporter.send(ModernText.parse(
                        "<!italic><" + ModernText.WARN + ">Você já tem uma denúncia aberta contra este jogador.")));
                return;
            }

            PlayerReport draft = new PlayerReport(
                    0L,
                    reporter.getId(),
                    reporter.getName(),
                    targetId,
                    displayTarget,
                    reason,
                    cleanDetails,
                    serverId(),
                    ReportStatus.OPEN,
                    null,
                    null,
                    java.time.Instant.now(),
                    java.time.Instant.now()
            );

            long id = data.insert(draft);
            runSync(reporterPlayer, () -> {
                reporter.setCooldown(Constant.REPORT_SEND_COOLDOWN_KEY, REPORT_COOLDOWN_MS);
                clearPending(reporterPlayer);
                reporter.send(ModernText.parse(
                        "<!italic><" + ModernText.SUCCESS + ">Denúncia <#F1F5F9>#" + id
                                + " <" + ModernText.SUCCESS + ">enviada. A equipe foi notificada."));
                reporterPlayer.playSound(reporterPlayer.getLocation(),
                        org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.6f);
                publishAlert(id, reporter.getName(), displayTarget, reason, cleanDetails);
            });
        });
    }

    public static void publishAlert(long id, String reporter, String target, ReportReason reason, String details) {
        String shortDetails = details == null || details.isBlank()
                ? "-" : details.substring(0, Math.min(120, details.length())).replace('|', ' ');
        String payload = id + "|" + reporter + "|" + target + "|" + reason.id() + "|"
                + serverId() + "|" + shortDetails;

        if (Core.getRedis() != null && Core.getRedis().isEnabled()) {
            Core.getRedis().publish(Constant.REDIS_NETWORK_REPORT_CHANNEL, payload);
        }
        notifyStaffLocal(id, reporter, target, reason, shortDetails);
    }

    public static void notifyStaffLocal(long id, String reporter, String target,
                                        ReportReason reason, String details) {
        var plugin = Core.getJavaPlugin();
        if (plugin == null) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player staff : Bukkit.getOnlinePlayers()) {
                Account acc = Core.getAccountController().of(staff.getUniqueId());
                if (acc == null || !acc.isStaffer()) continue;
                if (!staff.hasPermission("lobbyhost.reports")) continue;
                staff.sendMessage(ModernText.parse(
                        "<!italic><" + ModernText.ERROR + "><bold> REPORT #" + id + "</bold>"
                                + " <#64748B>| <#F1F5F9>" + reporter + " → " + target
                                + " <#64748B>| <" + ModernText.WARN + ">" + reason.label()
                                + " <#64748B>| <" + ModernText.MUTED + ">" + serverId()));
                if (details != null && !details.equals("-")) {
                    staff.sendMessage(ModernText.parse(ModernText.mini(details)));
                }
                staff.sendMessage(ModernText.parse(ModernText.hint("/reports — abrir painel")));
            }
        });
    }

    public static void deliverFromRedis(String message) {
        String[] p = message.split("\\|", 6);
        if (p.length < 5) return;
        try {
            long id = Long.parseLong(p[0].trim());
            String reporter = p[1];
            String target = p[2];
            ReportReason reason = ReportReason.fromInput(p[3]);
            if (reason == null) reason = ReportReason.OTHER;
            String details = p.length >= 6 ? p[5] : "-";
            notifyStaffLocal(id, reporter, target, reason, details);
        } catch (NumberFormatException ignored) {
        }
    }

    private static String sanitize(String details) {
        if (details == null) return "";
        String d = details.trim().replace('\n', ' ');
        if (d.length() > DETAILS_MAX) d = d.substring(0, DETAILS_MAX);
        return d;
    }

    private static void runSync(Player player, Runnable task) {
        if (player == null || !player.isOnline()) return;
        Bukkit.getScheduler().runTask(Core.getJavaPlugin(), task);
    }
}
