package com.minecraft.lobbyhost.bukkit.chat;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class StaffChatBridge {

    private StaffChatBridge() {}

    public static boolean canUseStaffChat(Account account, Player player) {
        return account != null && (account.isStaffer() || player.hasPermission("lobbyhost.staffchat"));
    }

    public static void send(Account sender, String message) {
        if (sender == null || message == null || message.isBlank()) return;
        String payload = sender.getColoredName() + "|" + LobbySettings.SERVER_CHAT_ID + "|" + sanitize(message);
        if (Core.getRedis() != null && Core.getRedis().isEnabled()) {
            Core.getRedis().publish(Constant.REDIS_PROXY_STAFFCHAT_CHANNEL, payload);
            return;
        }
        deliverLocal(sender.getColoredName(), LobbySettings.SERVER_CHAT_ID, message);
    }

    public static void deliverFromRedis(String coloredFrom, String server, String body) {
        Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> deliverLocal(coloredFrom, server, body));
    }

    private static void deliverLocal(String coloredFrom, String server, String body) {
        String line = LobbySettings.STAFFCHAT_PREFIX + coloredFrom + " §7(" + server + ")§7: §f" + body;
        Component c = Component.text(line);
        for (Player p : Bukkit.getOnlinePlayers()) {
            Account a = Core.getAccountController().of(p.getUniqueId());
            if (a == null) continue;
            if (!a.isStaffer() && !p.hasPermission("lobbyhost.staffchat")) continue;
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
