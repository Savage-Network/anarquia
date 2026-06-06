package com.minecraft.proxyhost.routing;

import com.minecraft.proxyhost.config.ConfigSection;
import com.minecraft.proxyhost.util.LegacyTexts;
import net.kyori.adventure.text.Component;

import java.util.List;

/** Bloqueia conexões ao backend de eventos enquanto o modo estiver em desenvolvimento. */
public final class EventosAccessGuard {

    private static String serverName = "eventos";
    private static boolean accessEnabled;
    private static List<String> blockedMessage = defaultMessage();

    private EventosAccessGuard() {}

    public static void load(ConfigSection config) {
        if (config == null) return;
        serverName = config.getString("routing.eventos-server", "eventos");
        accessEnabled = config.getBoolean("routing.eventos-access.enabled", false);
        blockedMessage = config.getStringList("routing.eventos-access.message", defaultMessage());
    }

    public static void setEnabled(boolean enabled) {
        accessEnabled = enabled;
    }

    public static boolean isOpen() {
        return accessEnabled;
    }

    public static String serverName() {
        return serverName;
    }

    public static boolean isBlockedTarget(String velocityServerName) {
        if (accessEnabled || velocityServerName == null || velocityServerName.isBlank()) {
            return false;
        }
        return serverName.equalsIgnoreCase(velocityServerName.trim());
    }

    public static Component blockedMessage() {
        return LegacyTexts.joinLines(LegacyTexts.colorize(blockedMessage));
    }

    private static List<String> defaultMessage() {
        return List.of(
                "&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                "&d&lEVENTOS",
                "",
                "&7O servidor de eventos ainda esta &eem desenvolvimento&7.",
                "&7Volte em breve!",
                "&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        );
    }
}
