package com.minecraft.eventos.service;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/** Kits built-in aplicados nos modos. */
public final class EventKitService {

    private EventKitService() {}

    public static void apply(Player player, String kitId) {
        if (player == null) return;
        PlayerInventory inv = player.getInventory();
        inv.clear();
        player.getInventory().setArmorContents(null);

        switch (kitId == null ? "" : kitId.toLowerCase()) {
            case "sumo" -> applySumo(inv);
            case "spleef" -> applySpleef(inv);
            case "tntrun", "tnt_run" -> applyTntRun(inv);
            case "blockparty", "block_party" -> { /* sem kit */ }
            case "parkour" -> applyParkour(inv);
            case "ffa", "deathmatch", "pvp" -> applyPvp(inv);
            case "archer" -> applyArcher(inv);
            default -> applyPvp(inv);
        }
    }

    private static void applySumo(PlayerInventory inv) {
        ItemStack stick = new ItemStack(Material.STICK);
        stick.addEnchantment(Enchantment.KNOCKBACK, 2);
        inv.setItem(0, stick);
    }

    private static void applySpleef(PlayerInventory inv) {
        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
        shovel.addEnchantment(Enchantment.EFFICIENCY, 5);
        shovel.addEnchantment(Enchantment.UNBREAKING, 3);
        inv.setItem(0, shovel);
    }

    private static void applyTntRun(PlayerInventory inv) {
        inv.setBoots(new ItemStack(Material.LEATHER_BOOTS));
    }

    private static void applyParkour(PlayerInventory inv) {
        inv.setBoots(enchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.FEATHER_FALLING, 4));
    }

    private static void applyPvp(PlayerInventory inv) {
        inv.setItem(0, enchant(new ItemStack(Material.DIAMOND_SWORD), Enchantment.SHARPNESS, 1));
        inv.setItem(1, new ItemStack(Material.GOLDEN_APPLE, 8));
        inv.setItem(2, new ItemStack(Material.COOKED_BEEF, 32));
        inv.setHelmet(new ItemStack(Material.IRON_HELMET));
        inv.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        inv.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        inv.setBoots(new ItemStack(Material.IRON_BOOTS));
    }

    private static void applyArcher(PlayerInventory inv) {
        inv.setItem(0, enchant(new ItemStack(Material.BOW), Enchantment.POWER, 1));
        inv.setItem(1, new ItemStack(Material.ARROW, 64));
        inv.setItem(2, new ItemStack(Material.STONE_SWORD));
    }

    private static ItemStack enchant(ItemStack item, Enchantment ench, int level) {
        item.addEnchantment(ench, level);
        return item;
    }
}
