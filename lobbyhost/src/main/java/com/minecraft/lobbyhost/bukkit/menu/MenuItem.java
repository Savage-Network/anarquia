package com.minecraft.lobbyhost.bukkit.menu;

import com.minecraft.lobbyhost.util.ModernText;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
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

    public MenuItem name(String legacyName) {
        return nameComponent(Component.text(legacyName));
    }

    public MenuItem miniName(String mini) {
        return nameComponent(ModernText.noItalic(ModernText.parse(mini)));
    }

    public MenuItem nameComponent(Component name) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(ModernText.noItalic(name));
            stack.setItemMeta(meta);
        }
        return this;
    }

    public MenuItem lore(String... legacyLines) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            for (String line : legacyLines) {
                lore.add(ModernText.noItalic(Component.text(line)));
            }
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return this;
    }

    public MenuItem miniLore(String... miniLines) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            for (String line : miniLines) {
                if (line == null || line.isEmpty()) {
                    lore.add(Component.empty());
                } else {
                    lore.add(ModernText.noItalic(ModernText.parse(line)));
                }
            }
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return this;
    }

    public MenuItem loreComponents(Component... lines) {
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.lore(Arrays.stream(lines).map(ModernText::noItalic).toList());
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
