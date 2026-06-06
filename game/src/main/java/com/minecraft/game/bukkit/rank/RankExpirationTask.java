package com.minecraft.game.bukkit.rank;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.util.AccountSync;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class RankExpirationTask extends BukkitRunnable {

    private final JavaPlugin plugin;

    public RankExpirationTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Account account : Core.getAccountController().list()) {
            if (!RankGrantService.refreshRankState(account)) continue;

            AccountSync.saveAndBroadcast(account);
            RankGrantService.refreshOnline(account);
            account.send("§cSeu rank temporario expirou. Voce voltou para §7"
                    + account.getRankType().name() + "§c.");
        }
    }

    public void start() {
        runTaskTimer(plugin, 20L * 30L, 20L * 30L);
    }
}
