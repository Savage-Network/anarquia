package com.minecraft.game.bukkit.semi;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatTagManager {

    private static final Map<UUID, Long> TAGGED_UNTIL = new ConcurrentHashMap<>();

    private CombatTagManager() {}

    public static void tag(Player player) {
        if (player == null) return;
        TAGGED_UNTIL.put(player.getUniqueId(), System.currentTimeMillis() + SemiAnarchyConfig.combatTagMs());
    }

    public static boolean isTagged(Player player) {
        if (player == null) return false;
        Long until = TAGGED_UNTIL.get(player.getUniqueId());
        if (until == null) return false;
        if (until <= System.currentTimeMillis()) {
            TAGGED_UNTIL.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public static long remainingSeconds(Player player) {
        Long until = TAGGED_UNTIL.get(player.getUniqueId());
        if (until == null) return 0;
        long left = until - System.currentTimeMillis();
        return left > 0 ? (left + 999) / 1000 : 0;
    }

    public static void clear(Player player) {
        if (player != null) TAGGED_UNTIL.remove(player.getUniqueId());
    }

    public static void startActionBarTask(GamePlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!isTagged(player)) continue;
                long sec = remainingSeconds(player);
                player.sendActionBar(Component.text("§c§l⚔ COMBATE §7(" + sec + "s)"));
            }
        }, 20L, 20L);
    }

    public static String blockedMessage(Player player) {
        return "§cVoce esta em combate! Aguarde " + remainingSeconds(player) + "s.";
    }
}
