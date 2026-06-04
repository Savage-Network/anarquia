package com.minecraft.lobbyhost.bukkit.manager;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.AccountController;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyScoreboard;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyTabList;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitTask;

/**
 * Mantem as tasks periodicas do plugin. Fase 1 cobre apenas o auto-save;
 * Fase 2 adiciona animacao de tags, follow de displays, etc.
 */
@RequiredArgsConstructor
public final class TaskManager {

    private final LobbyHostPlugin plugin;

    private BukkitTask autoSaveTask;
    private BukkitTask sidebarTask;
    private BukkitTask tabListTask;

    public void start() {
        long sidebarTicks = LobbySettings.SIDEBAR_UPDATE_TICKS;
        sidebarTask = plugin.getServer().getScheduler().runTaskTimer(plugin,
                LobbyScoreboard::updateOnlineCount, sidebarTicks, sidebarTicks);

        long tabTicks = LobbySettings.TABLIST_UPDATE_TICKS;
        tabListTask = plugin.getServer().getScheduler().runTaskTimer(plugin,
                LobbyTabList::refreshAll, tabTicks, tabTicks);
        // Auto-save a cada 5 minutos (apenas contas dirty)
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
        if (autoSaveTask != null && !autoSaveTask.isCancelled()) {
            autoSaveTask.cancel();
        }
        if (sidebarTask != null && !sidebarTask.isCancelled()) {
            sidebarTask.cancel();
        }
        if (tabListTask != null && !tabListTask.isCancelled()) {
            tabListTask.cancel();
        }
    }
}
