package com.minecraft.lobbyhost.bukkit.chat;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class StaffChatBridge {

    private StaffChatBridge() {}

    public static boolean canUseStaffChat(Account account, Player player) {
        if (account == null) return false;
        if (account.canUseStaffChat()) return true;
        return player != null && (
                player.hasPermission("game.staffchat")
                        || player.hasPermission("lobbyhost.staffchat")
                        || player.hasPermission("proxyhost.staffchat"));
    }

    public static void send(Account sender, String message) {
        if (sender == null || message == null || message.isBlank()) return;

        String coloredFrom = sender.getColoredName();
        String server = LobbySettings.SERVER_CHAT_ID;
        String body = sanitize(message);
        String payload = coloredFrom + "|" + server + "|" + body;

        if (useLocalFallback()) {
            deliverLocal(coloredFrom, server, body);
            return;
        }

        Player player = sender.player();
        if (player != null && player.isOnline()) {
            StaffChatMessenger.sendToProxy(player, payload);
            return;
        }

        deliverLocal(coloredFrom, server, body);
    }

    private static boolean useLocalFallback() {
        var plugin = Core.getJavaPlugin();
        if (plugin == null) return true;
        return plugin.getConfig().getBoolean("staffchat.local-fallback", false);
    }

    private static void deliverLocal(String coloredFrom, String server, String body) {
        String line = LobbySettings.STAFFCHAT_PREFIX + coloredFrom + " §7(" + server + ")§7: §f" + body;
        Component c = Component.text(line);
        for (Player p : Bukkit.getOnlinePlayers()) {
            Account a = Core.getAccountController().of(p.getUniqueId());
            if (a == null) continue;
            if (!canUseStaffChat(a, p)) continue;
            p.sendMessage(c);
        }
        Core.getLogger().info("[STAFF] " + strip(coloredFrom) + " @ " + server + ": " + body);
    }

    private static String sanitize(String msg) {
        return msg.replace('|', ' ');
    }

    private static String strip(String colored) {
        return colored == null ? "?" : colored.replaceAll("§.", "");
    }
}
