package com.minecraft.game.bukkit.manager;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.GameSettings;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.AccountController;
import com.minecraft.game.bukkit.display.GameScoreboard;
import com.minecraft.game.bukkit.display.GameTabList;
import com.minecraft.game.leaderboard.LeaderboardCache;
import com.minecraft.game.network.NetworkPollTask;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public final class TaskManager {

    private final GamePlugin plugin;

    private BukkitTask autoSaveTask;
    private BukkitTask sidebarTask;
    private BukkitTask tablistTask;
    private BukkitTask networkPollTask;
    private BukkitTask leaderboardTask;

    public void start() {
        LeaderboardCache.refreshAll();
        long sidebarTicks = GameSettings.SIDEBAR_UPDATE_TICKS;
        sidebarTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            GameScoreboard.refreshOnlineCount();
            GameScoreboard.refreshCombatTags();
        }, sidebarTicks, sidebarTicks);

        long tabTicks = GameSettings.TABLIST_UPDATE_TICKS;
        tablistTask = plugin.getServer().getScheduler().runTaskTimer(plugin,
                GameTabList::refreshAll, tabTicks, tabTicks);

        long networkTicks = GameSettings.NETWORK_POLL_TICKS;
        networkPollTask = new NetworkPollTask().runTaskTimerAsynchronously(
                plugin, 0L, networkTicks);

        leaderboardTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin,
                LeaderboardCache::refreshAll,
                GameSettings.LEADERBOARD_REFRESH_TICKS,
                GameSettings.LEADERBOARD_REFRESH_TICKS);

        long interval = 20L * 60L * 5L;
        autoSaveTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            AccountController controller = Core.getAccountController();
            if (controller == null) return;
            int saved = 0;
            for (Account account : controller.list()) {
                if (account.isDirty()) {
                    Core.getAccountData().save(account);
                    account.clearDirty();
                    saved++;
                }
            }
            if (saved > 0) {
                plugin.getLogger().info("Auto-save: " + saved + " conta(s) gravada(s).");
            }
        }, interval, interval);
    }

    public void stop() {
        if (autoSaveTask != null && !autoSaveTask.isCancelled()) autoSaveTask.cancel();
        if (sidebarTask != null && !sidebarTask.isCancelled()) sidebarTask.cancel();
        if (tablistTask != null && !tablistTask.isCancelled()) tablistTask.cancel();
        if (networkPollTask != null && !networkPollTask.isCancelled()) networkPollTask.cancel();
        if (leaderboardTask != null && !leaderboardTask.isCancelled()) leaderboardTask.cancel();
    }
}
