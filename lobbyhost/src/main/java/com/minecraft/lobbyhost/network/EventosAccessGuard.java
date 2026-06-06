package com.minecraft.lobbyhost.network;

import com.minecraft.lobbyhost.LobbySettings;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

/** Bloqueia fila/conexão ao backend de eventos enquanto o modo estiver fechado. */
public final class EventosAccessGuard {

    private static boolean accessEnabled;
    private static List<String> blockedMessage = defaultMessage();

    private EventosAccessGuard() {}

    public static void load(FileConfiguration config) {
        if (config == null) return;
        accessEnabled = config.getBoolean("routing.eventos-access.enabled", false);
        List<String> lines = config.getStringList("routing.eventos-access.message");
        blockedMessage = lines == null || lines.isEmpty() ? defaultMessage() : lines;
    }

    public static void syncFromNetwork(boolean enabled) {
        accessEnabled = enabled;
    }

    public static boolean isOpen() {
        return accessEnabled;
    }

    public static boolean isBlockedMode(String modeIdOrServer) {
        if (accessEnabled || modeIdOrServer == null || modeIdOrServer.isBlank()) return false;
        String id = modeIdOrServer.trim();
        return LobbySettings.EVENTOS_SERVER.equalsIgnoreCase(id) || "eventos".equalsIgnoreCase(id);
    }

    public static void sendBlocked(Player player) {
        if (player == null) return;
        for (String line : blockedMessage) {
            player.sendMessage(line.replace('&', '§'));
        }
    }

    private static List<String> defaultMessage() {
        return List.of(
                "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                "§d§lEVENTOS",
                "",
                "§7O servidor de eventos ainda esta §eem desenvolvimento§7.",
                "§7Volte em breve!",
                "§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        );
    }
}
