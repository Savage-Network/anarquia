package com.minecraft.game.bukkit.menu;

import com.minecraft.game.message.MenuTexts;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Menu chest base para Paper 1.21 (sem NMS).
 */
public abstract class Menu {

    protected final Player player;
    protected final String title;
    protected final int rows;
    protected final Menu parent;

    private final Map<Integer, MenuItem> contents = new HashMap<>();
    private Inventory inventory;

    protected Menu(Player player, String title, int rows, Menu parent) {
        this.player = player;
        this.title = title;
        this.rows = rows;
        this.parent = parent;
    }

    public abstract void build();

    public void open() {
        build();
        int size = rows * 9;
        inventory = Bukkit.createInventory(player, size, title);
        if (bordered()) {
            fillBorder();
        }
        contents.forEach((slot, item) -> inventory.setItem(slot, item.stack()));
        player.openInventory(inventory);
        MenuManager.track(player, this);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.6f, 1.2f);
    }

    public void reopen() {
        if (player.getOpenInventory().getTopInventory().equals(inventory)) {
            open();
        }
    }

    protected void clear() {
        contents.clear();
    }

    protected void set(int slot, MenuItem item) {
        contents.put(slot, item);
    }

    protected void fillIfEmpty(int slot, MenuItem item) {
        if (!contents.containsKey(slot)) {
            contents.put(slot, item);
        }
    }

    /** API publica para helpers de layout (ex.: {@code ShopMenuLayout}). */
    public final void place(int slot, MenuItem item) {
        set(slot, item);
    }

    public final void placeIfEmpty(int slot, MenuItem item) {
        fillIfEmpty(slot, item);
    }

    protected boolean bordered() {
        return true;
    }

    protected void fillBorder() {
        MenuItem glass = MenuItem.of(Material.GRAY_STAINED_GLASS_PANE).name(" ");
        int size = rows * 9;
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
                if (!contents.containsKey(i)) contents.put(i, glass);
            }
        }
    }

    protected void back(int slot) {
        if (parent == null) return;
        set(slot, MenuItem.of(Material.ARROW)
                .name(MenuTexts.legacy(player, "menus.common.back"))
                .lore(MenuTexts.legacy(player, "menus.common.back-lore"))
                .click(p -> parent.open()));
    }

    protected void closeButton(int slot) {
        set(slot, MenuItem.of(Material.BARRIER)
                .name(MenuTexts.legacy(player, "menus.common.close"))
                .click(p -> p.closeInventory()));
    }

    protected void clickSound() {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.4f);
    }

    protected void successSound() {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.6f);
    }

    public boolean isThis(Inventory top) {
        return inventory != null && inventory.equals(top);
    }

    public MenuItem itemAt(int slot) {
        return contents.get(slot);
    }

    public int getRows() {
        return rows;
    }

    public Player getPlayer() {
        return player;
    }
}
