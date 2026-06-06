package com.minecraft.game.network;

import com.minecraft.game.Core;
import com.minecraft.game.Constant;
import com.minecraft.game.account.Account;
import com.minecraft.game.backend.data.ActivityFeedData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public final class ActivityFeedService {

    private ActivityFeedService() {}

    public static void publish(String type, String actorName, String message, String serverId) {
        ActivityFeedData data = Core.getActivityFeedData();
        if (data == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            long id = data.insert(type, actorName, message, serverId);
            String payload = id + "|" + type + "|" + actorName + "|" + message + "|" + serverId;
            if (Core.getRedis() != null && Core.getRedis().isEnabled()) {
                Core.getRedis().publish(Constant.REDIS_ACTIVITY_FEED_CHANNEL, payload);
            } else {
                deliverLocal(type, actorName, message);
            }
        });
    }

    public static void deliverFromRedis(String payload) {
        String[] parts = payload.split("\\|", 5);
        if (parts.length < 4) return;
        deliverLocal(parts[1], parts[2], parts[3]);
    }

    private static void deliverLocal(String type, String actorName, String message) {
        String line = formatLine(type, actorName, message);
        Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                Account acc = Core.getAccountController().of(online.getUniqueId());
                if (acc == null || !acc.getToggle().isNotifyFeed()) continue;
                online.sendMessage(line);
            }
        });
    }

    public static String formatLine(String type, String actorName, String message) {
        return switch (type != null ? type : "") {
            case "killstreak" -> "§c§lFEED §f" + actorName + " §7" + message;
            case "clan" -> "§b§lFEED §f" + actorName + " §7" + message;
            case "join" -> "§a§lFEED §f" + actorName + " §7" + message;
            case "achievement" -> "§6§lFEED §f" + actorName + " §7" + message;
            default -> "§7§lFEED §f" + actorName + " §7" + message;
        };
    }

    public static List<ActivityFeedData.Entry> recent(int limit) {
        ActivityFeedData data = Core.getActivityFeedData();
        if (data == null) return List.of();
        return data.recent(limit);
    }
}
