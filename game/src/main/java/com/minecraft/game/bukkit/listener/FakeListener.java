package com.minecraft.game.bukkit.listener;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.fake.FakeService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class FakeListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            Account account = Core.getAccountController().of(event.getPlayer().getUniqueId());
            if (account != null && account.isFake()) {
                FakeService.refreshIfFake(account, event.getPlayer());
            }
        }, 15L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        // Mantém fake no JSON da conta entre servidores; não limpa no quit.
    }
}
