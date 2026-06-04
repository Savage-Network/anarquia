package com.minecraft.game.bukkit.semi;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.backend.sync.NetworkStatsSync;
import com.minecraft.game.account.context.objects.stats.GameStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class KillRewardService {

    private static final long FARM_WINDOW_MS = 600_000L;
    private static final int FARM_MAX_SAME_VICTIM = 3;

    private static final Map<UUID, Map<UUID, List<Long>>> RECENT_KILLS = new ConcurrentHashMap<>();

    private KillRewardService() {}

    public static void handleKill(Player killer, Player victim) {
        if (killer == null || victim == null || killer.equals(victim)) return;
        if (SpawnProtection.isProtected(victim.getLocation())) return;

        Account killerAcc = Core.getAccountController().of(killer.getUniqueId());
        Account victimAcc = Core.getAccountController().of(victim.getUniqueId());
        if (killerAcc == null || victimAcc == null) return;

        boolean farmed = isFarmKill(killer.getUniqueId(), victim.getUniqueId());
        registerKill(killer.getUniqueId(), victim.getUniqueId());

        GameStats kStats = killerAcc.getGameStats();
        GameStats vStats = victimAcc.getGameStats();

        kStats.setKills(kStats.getKills() + 1);
        kStats.setKillStreak(kStats.getKillStreak() + 1);
        if (kStats.getKillStreak() > kStats.getBestKillStreak()) {
            kStats.setBestKillStreak(kStats.getKillStreak());
        }

        int victimStreak = vStats.getKillStreak();
        vStats.setDeaths(vStats.getDeaths() + 1);
        vStats.setKillStreak(0);

        int reward = 0;
        if (!farmed) {
            reward = SemiAnarchyConfig.killRewardBase();
            if (victimStreak >= 3) {
                reward += Math.min(
                        SemiAnarchyConfig.maxKillStreakBonus(),
                        (victimStreak - 2) * SemiAnarchyConfig.killStreakBonus());
            }
            int streakBonus = Math.min(
                    SemiAnarchyConfig.maxKillStreakBonus(),
                    Math.max(0, kStats.getKillStreak() - 1) * 10);
            reward += streakBonus;
        }

        int bounty = vStats.getBounty();
        if (bounty > 0) {
            reward += bounty;
            vStats.setBounty(0);
            Bukkit.broadcast(net.kyori.adventure.text.Component.text(
                    "§6§lBOUNTY §7" + killer.getName() + " eliminou " + victim.getName()
                            + " §7e recebeu §a" + SemiAnarchyConfig.currencySymbol() + bounty));
        }

        if (reward > 0) {
            killerAcc.addGolds(reward);
            killer.sendMessage("§a+" + SemiAnarchyConfig.currencySymbol() + reward
                    + " §7por eliminar §f" + victim.getName()
                    + (farmed ? " §8(anti-farm)" : ""));
        }

        killerAcc.markDirty();
        victimAcc.markDirty();
        NetworkStatsSync.syncPlayer(killerAcc);
        NetworkStatsSync.syncPlayer(victimAcc);

        if (kStats.getKillStreak() >= 5) {
            Bukkit.broadcast(net.kyori.adventure.text.Component.text(
                    "§c§lKILLSTREAK §f" + killer.getName() + " §7esta com §c"
                            + kStats.getKillStreak() + " §7kills seguidas!"));
        }
    }

    private static boolean isFarmKill(UUID killer, UUID victim) {
        Map<UUID, List<Long>> map = RECENT_KILLS.get(killer);
        if (map == null) return false;
        List<Long> times = map.get(victim);
        if (times == null) return false;
        long cutoff = System.currentTimeMillis() - FARM_WINDOW_MS;
        int count = 0;
        for (Long t : times) {
            if (t >= cutoff) count++;
        }
        return count >= FARM_MAX_SAME_VICTIM;
    }

    private static void registerKill(UUID killer, UUID victim) {
        RECENT_KILLS.computeIfAbsent(killer, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(victim, v -> new ArrayList<>())
                .add(System.currentTimeMillis());
        Map<UUID, List<Long>> map = RECENT_KILLS.get(killer);
        long cutoff = System.currentTimeMillis() - FARM_WINDOW_MS;
        for (Iterator<Map.Entry<UUID, List<Long>>> it = map.entrySet().iterator(); it.hasNext(); ) {
            List<Long> times = it.next().getValue();
            times.removeIf(t -> t < cutoff);
            if (times.isEmpty()) it.remove();
        }
    }
}
