package com.minecraft.game.bukkit.menu;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class MenuItem {

    private final ItemStack stack;
    private Consumer<Player> click;

    private MenuItem(Material material, int amount) {
        this.stack = new ItemStack(material, amount);
    }

    public static MenuItem of(Material material) {
        return new MenuItem(material, 1);
    }

    public static MenuItem of(Material material, int amount) {
        return new MenuItem(material, amount);
    }

    public MenuItem name(String name) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            stack.setItemMeta(meta);
        }
        return this;
    }

    public MenuItem lore(String... lines) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            for (String line : lines) lore.add(line);
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return this;
    }

    public MenuItem glow() {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            stack.setItemMeta(meta);
        }
        return this;
    }

    public MenuItem skullOwner(String owner) {
        if (stack.getItemMeta() instanceof SkullMeta skull) {
            skull.setOwner(owner);
            stack.setItemMeta(skull);
        }
        return this;
    }

    public MenuItem click(Consumer<Player> action) {
        this.click = action;
        return this;
    }

    public ItemStack stack() {
        return stack;
    }

    public Consumer<Player> click() {
        return click;
    }
}
