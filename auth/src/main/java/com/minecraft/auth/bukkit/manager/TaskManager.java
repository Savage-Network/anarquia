package com.minecraft.auth.bukkit.manager;

import com.minecraft.auth.Core;
import com.minecraft.auth.AuthPlugin;
import com.minecraft.auth.account.Account;
import com.minecraft.auth.account.AccountController;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitTask;

/**
 * Mantem as tasks periodicas do plugin. Fase 1 cobre apenas o auto-save;
 * Fase 2 adiciona animacao de tags, follow de displays, etc.
 */
@RequiredArgsConstructor
public final class TaskManager {

    private final AuthPlugin plugin;

    private BukkitTask autoSaveTask;

    public void start() {
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
    }
}
