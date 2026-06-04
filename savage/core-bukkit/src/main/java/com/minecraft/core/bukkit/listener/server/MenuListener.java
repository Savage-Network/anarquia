package com.minecraft.core.bukkit.listener.server;

import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.api.item.click.ItemClick;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.AsyncUpdateEvent;
import com.minecraft.core.bukkit.manager.list.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class MenuListener implements Listener {

    private final MenuManager manager;

    public MenuListener() {
        manager = BukkitCore.getManager().getMenu();
    }

    // Atualizando os itens dos inventários
    @EventHandler
    public void onInventoryUpdate(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (Map.Entry<UUID, Menu> entry : manager.getCache().entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());

                if (player == null) continue;

                Menu menu = entry.getValue();

                if (menu == null || menu.getContents().isEmpty()) continue;

                for (Item item : menu.getContents().values()) {
                    if (item == null || item.getUpdater() == null) continue;

                    item.getUpdater().runUpdate(player.getOpenInventory());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        manager.remove(event.getPlayer().getUniqueId());

        //BukkitCore.getManager().getArcadeConfig().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.remove(event.getPlayer().getUniqueId());

        BukkitCore.getManager().getArcadeConfig().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        manager.remove(event.getPlayer().getUniqueId());

        BukkitCore.getManager().getArcadeConfig().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerClickItem(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();

            if (event.getClickedInventory() == null || event.getCurrentItem() == null) {
                event.setCancelled(true);
                return;
            }

            if (manager.contains(player.getUniqueId())) {
                org.bukkit.inventory.Inventory clicked = event.getClickedInventory();

                if (clicked.equals(player.getInventory())) {
                    event.setCancelled(true);
                    return;
                }

                Menu menu = manager.of(player.getUniqueId());

                ClickType clickType = event.getClick();

                ItemStack currentItem = event.getCurrentItem(), cursor = event.getCursor();

                if ((!menu.isAllowClickItemWithQuantity() && clickType.equals(ClickType.RIGHT) && currentItem.getAmount() > 1)
                        || clickType.equals(ClickType.NUMBER_KEY) || clickType.name().contains("DROP")) {
                    event.setCancelled(true);
                    return;
                }

                if (event.getRawSlot() > clicked.getSize()) {
                    event.setCancelled(false);
                    return;
                }

                if (menu.isProtectedSlot(event.getSlot())) {
                    event.setCancelled(true);
                    return;
                }

                if (clickType.name().startsWith("SHIFT") && !menu.isAllowShift()) {
                    event.setCancelled(true);
                    return;
                }

                if (cursor != null && !cursor.getType().equals(Material.AIR) && (menu.isProtectedSlot(event.getSlot()) || menu.isProtectedContent(currentItem))) {
                    event.setCancelled(true);
                    return;
                }

                Item item = menu.getContents().get(event.getSlot());

                if (item == null)
                    return;

                event.setCancelled(menu.isProtectedContent(event.getCurrentItem()) || !menu.isAllowClick());

                if (!menu.isAllowClick())
                    player.setItemOnCursor(null);

                /* Rodando o clickType */
                ItemClick itemClick = item.getClick();

                if (itemClick != null) itemClick.runClick(event);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            InventoryView view = event.getView();

            Player player = (Player) view.getPlayer();

            Menu menu = manager.of(player.getUniqueId());

            if (menu == null) return;

            event.setCancelled(!menu.isAllowDrag());
        }
    }

    @EventHandler
    public void onInteractInventory(InventoryCloseEvent event) {
        InventoryView view = event.getView();

        ItemStack cursor = view.getCursor();

        if (cursor != null)
            view.setCursor(null);
    }

    @EventHandler
    public void onPlayerUseItem(PlayerInteractEvent event) {
        if (event.hasItem()) {
            ItemStack stack = event.getItem();

            if (stack == null || stack.getType().equals(Material.AIR) || !stack.hasItemMeta()) return;
            if (!Item.exists(stack)) return;

            Item item = Item.convertItem(stack);

            if (event.getAction().name().startsWith("RIGHT") && item.getInteract() != null)
                item.getInteract().runInteract(event);
        }
    }
}
