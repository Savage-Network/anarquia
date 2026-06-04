package com.minecraft.lobbyhost.bukkit.menu;

import com.minecraft.lobbyhost.util.ModernText;
import net.kyori.adventure.text.Component;
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
    protected final Component title;
    protected final int rows;
    protected final Menu parent;

    private final Map<Integer, MenuItem> contents = new HashMap<>();
    private Inventory inventory;

    protected Menu(Player player, String miniTitle, int rows, Menu parent) {
        this(player, ModernText.parse(miniTitle), rows, parent);
    }

    protected Menu(Player player, Component title, int rows, Menu parent) {
        this.player = player;
        this.title = ModernText.noItalic(title);
        this.rows = rows;
        this.parent = parent;
    }

    public abstract void build();

    public void open() {
        build();
        int size = rows * 9;
        inventory = Bukkit.createInventory(player, size, title);
        fillBorder();
        contents.forEach((slot, item) -> inventory.setItem(slot, item.stack()));
        player.openInventory(inventory);
        MenuManager.track(player, this);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.35f);
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

    protected void fillBorder() {
        MenuItem glass = MenuItem.of(Material.BLACK_STAINED_GLASS_PANE).miniName("<black>");
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
                .miniName(ModernText.gradient("Voltar", ModernText.ACCENT_A, ModernText.ACCENT_B))
                .miniLore(ModernText.mini("Retornar ao menu anterior"))
                .click(p -> parent.open()));
    }

    protected void closeButton(int slot) {
        set(slot, MenuItem.of(Material.BARRIER)
                .miniName("<!italic><" + ModernText.ERROR + "><bold>Fechar</bold>")
                .click(p -> p.closeInventory()));
    }

    protected void clickSound() {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.5f);
    }

    protected void successSound() {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.45f, 1.7f);
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
}
