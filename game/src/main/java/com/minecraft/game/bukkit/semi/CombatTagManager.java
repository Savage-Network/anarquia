package com.minecraft.game.bukkit.semi;

import com.minecraft.game.GamePlugin;
import com.minecraft.game.bukkit.display.GameScoreboard;
import com.minecraft.game.message.LanguageService;
import com.minecraft.game.message.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatTagManager {

    private static final Map<UUID, CombatTag> TAGS = new ConcurrentHashMap<>();

    private CombatTagManager() {}

    public static void tagPair(Player first, Player second) {
        if (first == null || second == null || first.equals(second)) return;
        tagPlayer(first, second);
        tagPlayer(second, first);
    }

    private static void tagPlayer(Player player, Player opponent) {
        if (player == null || opponent == null) return;

        boolean wasTagged = isTagged(player);
        long until = System.currentTimeMillis() + SemiAnarchyConfig.combatTagMs();
        CombatTag existing = getTag(player);
        long started = existing != null ? existing.startedMs() : System.currentTimeMillis();
        TAGS.put(player.getUniqueId(), new CombatTag(
                until,
                opponent.getUniqueId(),
                opponent.getName(),
                started
        ));

        if (!wasTagged) {
            notifyEnterCombat(player);
        }
    }

    private static void notifyEnterCombat(Player player) {
        player.sendMessage(Messages.prefixed("combat.tagged", LanguageService.of(player), Map.of(
                "seconds", String.valueOf(SemiAnarchyConfig.combatTagSeconds())
        )));
        player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.6f, 1.3f);
    }

    public static CombatTag getTag(Player player) {
        if (player == null) return null;
        CombatTag tag = TAGS.get(player.getUniqueId());
        if (tag == null || tag.isExpired()) {
            if (tag != null) TAGS.remove(player.getUniqueId());
            return null;
        }
        return tag;
    }

    public static boolean isTagged(Player player) {
        return getTag(player) != null;
    }

    public static long remainingSeconds(Player player) {
        CombatTag tag = getTag(player);
        if (tag == null) return 0;
        long left = tag.untilMs() - System.currentTimeMillis();
        return left > 0 ? (left + 999) / 1000 : 0;
    }

    public static UUID getOpponentId(Player player) {
        CombatTag tag = getTag(player);
        return tag != null ? tag.opponentId() : null;
    }

    public static String getOpponentName(Player player) {
        CombatTag tag = getTag(player);
        return tag != null ? tag.opponentName() : null;
    }

    public static void clear(Player player) {
        if (player != null) TAGS.remove(player.getUniqueId());
    }

    public static void startActionBarTask(GamePlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
                CombatTag tag = TAGS.get(player.getUniqueId());
                if (tag == null) continue;

                if (tag.untilMs() <= now) {
                    TAGS.remove(player.getUniqueId());
                    player.sendMessage(Messages.prefixed("combat.tag-expired", LanguageService.of(player)));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.6f);
                    GameScoreboard.refreshCombatTags();
                    continue;
                }

                long sec = remainingSeconds(player);
                String opponent = tag.opponentName() != null ? tag.opponentName() : "?";
                player.sendActionBar(Component.text(
                        "§c§l⚔ COMBATE §7(" + sec + "s) §8vs §f" + opponent));
            }
        }, 20L, 20L);
    }

    public static String blockedMessage(Player player) {
        return Messages.legacy("combat.blocked", LanguageService.of(player), Map.of(
                "seconds", String.valueOf(remainingSeconds(player))
        ));
    }

    public static boolean blockIfTagged(Player player) {
        if (!isTagged(player)) return false;
        player.sendMessage(Messages.prefixed("combat.blocked", LanguageService.of(player), Map.of(
                "seconds", String.valueOf(remainingSeconds(player))
        )));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1f);
        return true;
    }
}
