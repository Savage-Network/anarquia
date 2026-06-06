package com.minecraft.proxyhost.proxy;

import com.minecraft.proxyhost.account.context.objects.tag.LegacyHex;
import com.minecraft.proxyhost.util.LegacyTexts;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Action bar com countdown hex enquanto a manutenção está agendada.
 */
public final class MaintenanceActionBar {

    private static final LegacyComponentSerializer LEGACY_HEX = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private MaintenanceActionBar() {}

    public static void broadcast(ProxyServer proxy) {
        if (proxy == null || !MaintenanceScheduler.isScheduled()) {
            clear(proxy);
            return;
        }
        ProxySettingsManager settings = ProxySettingsManager.get();
        if (settings == null || !settings.isCountdownActionBarEnabled()) {
            clear(proxy);
            return;
        }
        Component bar = build(settings);
        for (Player player : proxy.getAllPlayers()) {
            player.sendActionBar(bar);
        }
    }

    public static void sync(Player player) {
        if (player == null) return;
        if (!MaintenanceScheduler.isScheduled()) {
            player.sendActionBar(Component.empty());
            return;
        }
        ProxySettingsManager settings = ProxySettingsManager.get();
        if (settings == null || !settings.isCountdownActionBarEnabled()) {
            player.sendActionBar(Component.empty());
            return;
        }
        player.sendActionBar(build(settings));
    }

    public static void clear(ProxyServer proxy) {
        if (proxy == null) return;
        for (Player player : proxy.getAllPlayers()) {
            player.sendActionBar(Component.empty());
        }
    }

    public static Component build(ProxySettingsManager settings) {
        long remaining = MaintenanceScheduler.remainingMs();
        int seconds = (int) Math.ceil(remaining / 1000.0);

        String mainHex = normalizeHex(settings.getCountdownActionBarColor(), "#FF8C42");
        String urgentHex = normalizeHex(settings.getCountdownActionBarUrgentColor(), "#FF6B6B");
        boolean urgent = seconds <= settings.getCountdownActionBarUrgentSeconds();
        String timeColor = urgent ? urgentHex : mainHex;

        String clock = formatClock(remaining);
        String reason = trim(MaintenanceScheduler.getScheduledReason(), 48);

        String legacy = LegacyHex.of(mainHex) + "⚠ "
                + LegacyHex.DARK_GRAY + "Manutenção em "
                + LegacyHex.of(timeColor) + "§l" + clock
                + LegacyHex.DARK_GRAY + " · "
                + LegacyHex.WHITE + reason;

        return LEGACY_HEX.deserialize(legacy);
    }

    /** Relógio legível: MM:SS ou H:MM:SS */
    public static String formatClock(long remainingMs) {
        long totalSec = Math.max(0, (remainingMs + 999) / 1000);
        long h = totalSec / 3600;
        long m = (totalSec % 3600) / 60;
        long s = totalSec % 60;
        if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        }
        return String.format("%02d:%02d", m, s);
    }

    private static String normalizeHex(String input, String fallback) {
        if (input == null || input.isBlank()) return fallback;
        String h = input.trim();
        if (!h.startsWith("#")) h = "#" + h;
        return h.length() == 7 ? h : fallback;
    }

    private static String trim(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }
}
