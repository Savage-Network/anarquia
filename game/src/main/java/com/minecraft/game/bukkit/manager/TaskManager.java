package com.minecraft.game.bukkit.manager;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.GameSettings;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.AccountController;
import com.minecraft.game.bukkit.display.GameScoreboard;
import com.minecraft.game.bukkit.display.GameTabList;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public final class TaskManager {

    private final GamePlugin plugin;

    private BukkitTask autoSaveTask;
    private BukkitTask sidebarTask;
    private BukkitTask tabListTask;

    public void start() {
        long sidebarTicks = GameSettings.SIDEBAR_UPDATE_TICKS;
        sidebarTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            GameScoreboard.refreshOnlineCount();
            GameScoreboard.refreshCombatTags();
        }, sidebarTicks, sidebarTicks);

        long tabTicks = GameSettings.TABLIST_UPDATE_TICKS;
        tabListTask = plugin.getServer().getScheduler().runTaskTimer(plugin,
                GameTabList::refreshAll, tabTicks, tabTicks);

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
        if (tabListTask != null && !tabListTask.isCancelled()) tabListTask.cancel();
    }
}
