package com.minecraft.game.bukkit.kit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Map;

public record KitSlotItem(int slot, ItemStack stack) {

    public static KitSlotItem fromSection(ConfigurationSection sec) {
        Material mat = Material.matchMaterial(sec.getString("material", "STONE").toUpperCase(Locale.ROOT));
        if (mat == null || !mat.isItem()) mat = Material.STONE;
        int amount = Math.max(1, Math.min(64, sec.getInt("amount", 1)));
        int slot = Math.max(0, Math.min(35, sec.getInt("slot", 0)));

        ItemStack stack = new ItemStack(mat, amount);
        ConfigurationSection ench = sec.getConfigurationSection("enchantments");
        if (ench != null) {
            var meta = stack.getItemMeta();
            if (meta != null) {
                for (String key : ench.getKeys(false)) {
                    Enchantment enchantment = Enchantment.getByName(key.toUpperCase(Locale.ROOT));
                    if (enchantment != null) {
                        meta.addEnchant(enchantment, ench.getInt(key, 1), true);
                    }
                }
                stack.setItemMeta(meta);
            }
        }
        return new KitSlotItem(slot, stack);
    }

    public static KitSlotItem fromMap(Map<?, ?> map) {
        ConfigurationSection sec = null;
        if (map.get("material") != null) {
            String material = String.valueOf(map.get("material"));
            int amount = map.get("amount") instanceof Number n ? n.intValue() : 1;
            int slot = map.get("slot") instanceof Number s ? s.intValue() : 0;
            Material mat = Material.matchMaterial(material.toUpperCase(Locale.ROOT));
            if (mat == null || !mat.isItem()) mat = Material.STONE;
            ItemStack stack = new ItemStack(mat, Math.max(1, Math.min(64, amount)));
            if (map.get("enchantments") instanceof Map<?, ?> enchMap) {
                var meta = stack.getItemMeta();
                if (meta != null) {
                    for (Map.Entry<?, ?> e : enchMap.entrySet()) {
                        Enchantment ench = Enchantment.getByName(String.valueOf(e.getKey()).toUpperCase(Locale.ROOT));
                        if (ench != null) {
                            int lvl = e.getValue() instanceof Number n ? n.intValue() : 1;
                            meta.addEnchant(ench, lvl, true);
                        }
                    }
                    stack.setItemMeta(meta);
                }
            }
            return new KitSlotItem(Math.max(0, Math.min(35, slot)), stack);
        }
        return new KitSlotItem(0, new ItemStack(Material.STONE));
    }
}
