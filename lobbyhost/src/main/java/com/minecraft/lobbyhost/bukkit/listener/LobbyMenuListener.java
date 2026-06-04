package com.minecraft.lobbyhost.bukkit.listener;

import com.minecraft.lobbyhost.bukkit.menu.LobbyMenuHolder;
import com.minecraft.lobbyhost.bukkit.menu.LobbyMenus;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Cliques em menus GUI do lobbyhost.
 */
public final class LobbyMenuListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof LobbyMenuHolder holder)) return;

        event.setCancelled(true);
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        if (current.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        if ("navigation".equals(holder.getMenuId())) {
            LobbyMenus.handleNavigationClick(player, current);
        } else if (holder.getMenuId() != null && holder.getMenuId().startsWith("cosmetics")) {
            com.minecraft.lobbyhost.bukkit.menu.CosmeticMenus.handleClick(player, holder, current, event.getSlot());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof LobbyMenuHolder) {
            event.setCancelled(true);
        }
    }
}
