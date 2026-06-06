package com.minecraft.game.bukkit.chat;

import com.minecraft.game.Core;
import com.minecraft.game.GameSettings;
import com.minecraft.game.account.Account;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class StaffChatBridge {

    private StaffChatBridge() {}

    public static boolean canUseStaffChat(Account account, Player player) {
        return account != null && (account.isStaffer() || player.hasPermission("game.staffchat"));
    }

    public static void send(Account sender, String message) {
        if (sender == null || message == null || message.isBlank()) return;

        String coloredFrom = sender.getColoredName();
        String server = GameSettings.SERVER_CHAT_ID;
        String body = message.replace('|', ' ');
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
        Component c = Component.text(GameSettings.STAFFCHAT_PREFIX + coloredFrom
                + " §7(" + server + ")§7: §f" + body);
        for (Player p : Bukkit.getOnlinePlayers()) {
            Account a = Core.getAccountController().of(p.getUniqueId());
            if (a == null) continue;
            if (!a.isStaffer() && !p.hasPermission("game.staffchat")) continue;
            p.sendMessage(c);
        }
        Core.getLogger().info("[STAFF] " + coloredFrom + " @ " + server + ": " + body);
    }
}
