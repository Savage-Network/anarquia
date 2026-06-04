package com.minecraft.lobbyhost.bukkit.listener;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.vanish.VanishService;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class VanishListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoinHide(PlayerJoinEvent event) {
        VanishService.applyForJoining(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinAutoVanish(PlayerJoinEvent event) {
        Core.getJavaPlugin().getServer().getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            if (!event.getPlayer().isOnline()) return;
            Account account = Core.getAccountController().of(event.getPlayer().getUniqueId());
            VanishService.applyAutoOnJoin(event.getPlayer(), account);
        }, 3L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (VanishService.isVanished(event.getPlayer())) {
            event.quitMessage(null);
        }
        VanishService.onQuit(event.getPlayer());
    }
}
