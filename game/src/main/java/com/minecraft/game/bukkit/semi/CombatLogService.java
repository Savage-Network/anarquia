package com.minecraft.game.bukkit.semi;

import com.minecraft.game.Core;
import com.minecraft.game.GameSettings;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.stats.GameStats;
import com.minecraft.game.backend.sync.NetworkStatsSync;
import com.minecraft.game.message.LanguageService;
import com.minecraft.game.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/** Punicao e efeitos de combat log (deslogar/kick em combate). */
public final class CombatLogService {

    private CombatLogService() {}

    public static void punish(Player player) {
        if (player == null || !CombatTagManager.isTagged(player)) return;

        CombatTag tag = CombatTagManager.getTag(player);
        Location loc = player.getLocation().clone();
        Player killer = resolveKiller(tag);

        if (GameSettings.COMBAT_LOG_DROP_ITEMS) {
            dropAll(player, loc);
        }

        if (killer != null && GameSettings.COMBAT_LOG_KILL_CREDIT) {
            KillRewardService.handleKill(killer, player);
        } else {
            applyDeathStats(player);
        }

        if (GameSettings.COMBAT_LOG_BROADCAST) {
            broadcastCombatLog(player, tag, killer);
        }

        CombatLogNpcService.spawnMarker(player, tag);

        if (killer != null && killer.isOnline()) {
            killer.sendMessage(Messages.prefixed("combat.combat-log-killer", LanguageService.of(killer),
                    Map.of("player", player.getName())));
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
        }

        CombatTagManager.clear(player);
    }

    private static Player resolveKiller(CombatTag tag) {
        if (tag == null || tag.opponentId() == null) return null;
        Player killer = Bukkit.getPlayer(tag.opponentId());
        if (killer == null || !killer.isOnline()) return null;
        long sinceTag = System.currentTimeMillis() - tag.startedMs();
        if (sinceTag > GameSettings.COMBAT_OPPONENT_CREDIT_MS) return null;
        return killer;
    }

    private static void applyDeathStats(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;
        GameStats stats = account.getGameStats();
        stats.setDeaths(stats.getDeaths() + 1);
        stats.setKillStreak(0);
        account.markDirty();
        NetworkStatsSync.syncPlayer(account);
    }

    private static void dropAll(Player player, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        for (ItemStack item : player.getInventory().getContents()) {
            dropItem(world, loc, item);
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            dropItem(world, loc, item);
        }
        dropItem(world, loc, player.getInventory().getItemInOffHand());

        int exp = player.getTotalExperience();
        if (exp > 0) {
            ExperienceOrb orb = world.spawn(loc, ExperienceOrb.class);
            orb.setExperience(exp);
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setTotalExperience(0);
        player.setExp(0);
        player.setLevel(0);
    }

    private static void dropItem(World world, Location loc, ItemStack item) {
        if (item == null || item.getType().isAir()) return;
        world.dropItemNaturally(loc, item.clone());
    }

    private static void broadcastCombatLog(Player victim, CombatTag tag, Player killer) {
        String opponent = tag != null && tag.opponentName() != null ? tag.opponentName() : "?";
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (killer != null) {
                online.sendMessage(Messages.prefixed("combat.combat-log-broadcast-killer", LanguageService.of(online), Map.of(
                        "player", victim.getName(),
                        "killer", killer.getName()
                )));
            } else {
                online.sendMessage(Messages.prefixed("combat.combat-log-broadcast", LanguageService.of(online), Map.of(
                        "player", victim.getName(),
                        "opponent", opponent
                )));
            }
        }
    }

    public static void denyTeleport(Player player) {
        player.sendMessage(Messages.prefixed("combat.teleport-blocked", LanguageService.of(player)));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1f);
    }

    public static void denyPearl(Player player) {
        player.sendMessage(Messages.prefixed("combat.pearl-blocked", LanguageService.of(player)));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1f);
    }

    public static void denyChorus(Player player) {
        player.sendMessage(Messages.prefixed("combat.chorus-blocked", LanguageService.of(player)));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1f);
    }
}
