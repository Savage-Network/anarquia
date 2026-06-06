package com.minecraft.game.network;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.backend.data.NetworkPresenceData;
import com.minecraft.game.util.ModernText;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public final class NotificationService {

    private NotificationService() {}

    public static void notifyFriendOnline(Account viewer, String friendName, String serverLabel) {
        if (viewer == null || !viewer.getToggle().isNotifyFriendOnline()) return;
        viewer.send("§a§l+ §f" + friendName + " §7entrou em §e" + serverLabel);
    }

    public static void notifyBountyPlaced(Account target, int amount, String placerName) {
        if (target == null || !target.getToggle().isNotifyBountyOnMe()) return;
        target.send("§6§lBOUNTY §cRecompensa de §a" + amount + " §cna sua cabeça"
                + (placerName != null ? " §7(por " + placerName + ")" : "") + "!");
    }

    public static void notifyClanActivity(Account member, String message) {
        if (member == null || !member.getToggle().isNotifyClanActivity()) return;
        member.send("§b§lCLÃ §7" + message);
    }

    public static void broadcastKillStreak(String killerName, int streak) {
        if (streak < 3) return;
        Component msg = ModernText.parse("<!italic><red><bold>KILLSTREAK</bold></red> <white>"
                + killerName + "</white> <gray>esta com <red>" + streak + "</red> kills!</gray>");
        for (Player online : Bukkit.getOnlinePlayers()) {
            Account acc = Core.getAccountController().of(online.getUniqueId());
            if (acc != null && !acc.getToggle().isNotifyKillStreakGlobal()) continue;
            online.sendMessage(msg);
        }
    }

    public static void broadcastAchievement(String playerName, String achievementTitle) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            Account acc = Core.getAccountController().of(online.getUniqueId());
            if (acc != null && !acc.getToggle().isNotifyFeed()) continue;
            online.sendMessage("§6§lFEED §f" + playerName + " §7desbloqueou §e" + achievementTitle);
        }
    }

    public static Optional<NetworkPresenceData.Presence> findPresence(UUID playerId) {
        if (Core.getNetworkPresenceData() == null || playerId == null) return Optional.empty();
        return Core.getNetworkPresenceData().findOnline(playerId);
    }
}
