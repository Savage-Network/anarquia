package com.minecraft.game.bukkit.menu.listener;

import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.bukkit.menu.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Menu menu = MenuManager.get(player);
        if (menu == null || !menu.isThis(event.getView().getTopInventory())) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        MenuItem item = menu.itemAt(event.getSlot());
        if (item != null && item.click() != null) {
            item.click().accept(player);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Menu menu = MenuManager.get(player);
        if (menu != null && menu.isThis(event.getView().getTopInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            MenuManager.untrack(player);
        }
    }
}
