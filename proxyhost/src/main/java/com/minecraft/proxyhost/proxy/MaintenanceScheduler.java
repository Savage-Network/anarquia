package com.minecraft.proxyhost.proxy;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.network.BroadcastService;
import com.minecraft.proxyhost.util.LegacyTexts;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Agenda manutenção com avisos countdown para todos os jogadores online.
 */
public final class MaintenanceScheduler {

    private static final int[] WARN_SECONDS = {600, 300, 120, 60, 30, 15, 10, 5};
    private static final Pattern DURATION = Pattern.compile("(\\d+)\\s*(ms|s|sec|secs|m|min|mins|h|hr|hrs)?", Pattern.CASE_INSENSITIVE);

    private static ScheduledTask task;
    private static final Set<Integer> sentWarnings = ConcurrentHashMap.newKeySet();

    @Getter private static long activateAtMs;
    @Getter private static String scheduledReason = "";

    private MaintenanceScheduler() {}

    public static boolean isScheduled() {
        return task != null && activateAtMs > System.currentTimeMillis();
    }

    public static long remainingMs() {
        return Math.max(0, activateAtMs - System.currentTimeMillis());
    }

    public static void schedule(ProxyServer proxy, long delayMs, String reason, CommandSource initiator) {
        cancel(proxy, false);
        if (delayMs < 5_000L) delayMs = 5_000L;

        activateAtMs = System.currentTimeMillis() + delayMs;
        scheduledReason = reason == null || reason.isBlank()
                ? "Atualização da rede"
                : reason.trim();
        sentWarnings.clear();

        String who = initiator instanceof Player p ? p.getUsername() : "Console";
        broadcastAll(proxy, legacy(
                "&6&lSavageMC &8| &eManutenção agendada em &f"
                        + formatDuration(delayMs)
                        + "&7. Motivo: &f" + scheduledReason
                        + " &7(por &e" + who + "&7)"));

        task = proxy.getScheduler()
                .buildTask(Core.getPlugin(), () -> tick(proxy))
                .repeat(1, TimeUnit.SECONDS)
                .schedule();

        MaintenanceActionBar.broadcast(proxy);
    }

    public static void cancel(ProxyServer proxy, boolean notify) {
        if (task != null) {
            task.cancel();
            task = null;
        }
        activateAtMs = 0;
        scheduledReason = "";
        sentWarnings.clear();
        MaintenanceActionBar.clear(proxy);
        if (notify && proxy != null) {
            broadcastAll(proxy, legacy("&a[Proxy] &7Manutenção agendada &acancelada&7."));
        }
    }

    public static long parseDuration(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Informe um tempo (ex: 5m, 30s, 1h).");
        }
        Matcher m = DURATION.matcher(raw.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("Formato invalido. Use 5m, 30s ou 1h.");
        }
        long value = Long.parseLong(m.group(1));
        String unit = m.group(2) == null ? "m" : m.group(2).toLowerCase(Locale.ROOT);
        return switch (unit) {
            case "ms" -> value;
            case "s", "sec", "secs" -> value * 1_000L;
            case "h", "hr", "hrs" -> value * 3_600_000L;
            default -> value * 60_000L;
        };
    }

    public static String formatDuration(long ms) {
        if (ms >= 3_600_000L) return (ms / 3_600_000L) + "h";
        if (ms >= 60_000L) return (ms / 60_000L) + "min";
        if (ms >= 1_000L) return (ms / 1_000L) + "s";
        return ms + "ms";
    }

    private static void tick(ProxyServer proxy) {
        long remaining = remainingMs();
        if (remaining <= 0) {
            activateMaintenance(proxy);
            return;
        }

        int seconds = (int) Math.ceil(remaining / 1000.0);
        for (int warn : WARN_SECONDS) {
            if (seconds <= warn && sentWarnings.add(warn)) {
                broadcastAll(proxy, legacy(
                        "&6&lSavageMC &8| &cManutenção em &f"
                                + formatCountdown(seconds)
                                + "&7. &f" + scheduledReason));
            }
        }

        MaintenanceActionBar.broadcast(proxy);
    }

    private static void activateMaintenance(ProxyServer proxy) {
        MaintenanceActionBar.clear(proxy);
        cancel(proxy, false);
        ProxySettingsManager settings = ProxySettingsManager.get();
        if (settings == null) return;

        settings.setMaintenanceReason(scheduledReason);
        settings.setMaintenanceEnabled(true);
        settings.kickNonStaff(proxy);
        NetworkPublisher.publishStatusNow();

        broadcastAll(proxy, legacy(
                "&c&lMANUTENÇÃO INICIADA &7— apenas quem tem acesso autorizado permanece online."));
        BroadcastService.sendBroadcast(
                "§cManutenção iniciada. Motivo: §f" + settings.getMaintenanceReason());
    }

    private static String formatCountdown(int seconds) {
        if (seconds >= 3600) return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
        if (seconds >= 60) return (seconds / 60) + "min " + (seconds % 60) + "s";
        return seconds + "s";
    }

    private static void broadcastAll(ProxyServer proxy, Component message) {
        for (Player player : proxy.getAllPlayers()) {
            player.sendMessage(message);
        }
    }

    private static Component legacy(String text) {
        return LegacyTexts.component(text);
    }
}
