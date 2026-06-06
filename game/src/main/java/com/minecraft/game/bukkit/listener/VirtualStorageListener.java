package com.minecraft.game.bukkit.listener;

import com.minecraft.game.bukkit.storage.PlayerVaultService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class VirtualStorageListener implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (PlayerVaultService.type(player) == null) return;
        PlayerVaultService.handleClose(player, event.getInventory());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (PlayerVaultService.type(player) == null) return;
        PlayerVaultService.handleClose(player, player.getOpenInventory().getTopInventory());
    }
}
