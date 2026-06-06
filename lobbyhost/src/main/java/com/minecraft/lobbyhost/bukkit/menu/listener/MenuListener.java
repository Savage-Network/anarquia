package com.minecraft.lobbyhost.bukkit.menu.listener;

import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.bukkit.menu.MenuManager;
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
        if (item == null) return;

        if (event.getClick().isRightClick()) {
            if (item.rightClick() != null) {
                item.rightClick().accept(player);
            }
            return;
        }

        if (item.click() != null) {
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
