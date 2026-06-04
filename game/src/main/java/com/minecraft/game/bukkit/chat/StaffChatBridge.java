package com.minecraft.game.bukkit.chat;

import com.minecraft.game.Constant;
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
        String payload = sender.getColoredName() + "|" + GameSettings.SERVER_CHAT_ID + "|"
                + message.replace('|', ' ');
        if (Core.getRedis() != null && Core.getRedis().isEnabled()) {
            Core.getRedis().publish(Constant.REDIS_PROXY_STAFFCHAT_CHANNEL, payload);
            return;
        }
        deliverLocal(sender.getColoredName(), GameSettings.SERVER_CHAT_ID, message);
    }

    public static void deliverFromRedis(String coloredFrom, String server, String body) {
        Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> deliverLocal(coloredFrom, server, body));
    }

    private static void deliverLocal(String coloredFrom, String server, String body) {
        Component c = Component.text(GameSettings.STAFFCHAT_PREFIX + coloredFrom
                + " §7(" + server + ")§7: §f" + body);
        for (Player p : Bukkit.getOnlinePlayers()) {
            Account a = Core.getAccountController().of(p.getUniqueId());
            if (a == null) continue;
            if (!a.isStaffer() && !p.hasPermission("game.staffchat")) continue;
            if (!a.getToggle().isAcStaffAlerts()) continue;
            p.sendMessage(c);
        }
        Core.getLogger().info("[STAFF] " + coloredFrom + " @ " + server + ": " + body);
    }
}
