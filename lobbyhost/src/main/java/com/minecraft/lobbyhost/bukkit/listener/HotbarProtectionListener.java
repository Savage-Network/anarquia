package com.minecraft.lobbyhost.bukkit.listener;

import com.minecraft.lobbyhost.bukkit.lobby.HotbarKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Prevents players from moving or dropping items that belong to the lobby hotbar.
 * Items are identified by the PersistentDataContainer key {@link HotbarKeys#SLOT_KEY}.
 */
public final class HotbarProtectionListener implements Listener {

    /**
     * Cancels any inventory click that would move a hotbar item.
     * This includes shift‑clicks, number‑key swaps, and drag‑and‑drop actions.
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        // Only care about player inventories
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        // Verify the item is a registered hotbar item
        if (HotbarKeys.readSlot(clicked) == null) return;

        // The slot index for the hotbar is 0‑8 in a player inventory.
        // If the click involves a hotbar slot, cancel the action.
        int slot = event.getSlot();
        if (slot >= 0 && slot <= 8) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels dragging a hotbar item to another inventory slot.
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        // Check each slot being dragged to – if any is a hotbar slot containing a protected item, cancel.
        for (int slot : event.getRawSlots()) {
            if (slot >= 0 && slot <= 8) {
                ItemStack cursor = event.getOldCursor();
                if (cursor != null && HotbarKeys.readSlot(cursor) != null) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * Prevents dropping a hotbar item from the hotbar.
     */
    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (item != null && HotbarKeys.readSlot(item) != null) {
            event.setCancelled(true);
        }
    }
}
