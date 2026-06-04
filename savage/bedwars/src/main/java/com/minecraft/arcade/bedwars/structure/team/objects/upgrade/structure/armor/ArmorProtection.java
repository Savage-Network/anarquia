package com.minecraft.arcade.bedwars.structure.team.objects.upgrade.structure.armor;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@Getter
@AllArgsConstructor
public enum ArmorProtection {

    NONE("", 0),

    ONE("I", 2),
    TWO("II", 4),
    THREE("III", 8),
    FOUR("IV", 16);

    private final String tag;
    private final int price;

    public String getName() {
        return "Proteção " + tag;
    }

    public boolean isHighest() {
        return this == FOUR;
    }

    public int getAdjustedPrice(Arena arena) {
        if (arena.getSlot().ordinal() < Slot.TRIO.ordinal()) return price;

        switch (this) {
            case ONE:
                return 5;
            case TWO:
                return 10;
            case THREE:
                return 20;
            case FOUR:
                return 30;
            default:
                return price;
        }
    }

    public void applyEnchantment(Player player) {
        // Aplicar diretamente a cada peça de armadura equipada
        PlayerInventory inv = player.getInventory();
        
        // Aplicar a cada peça de armadura
        applyToArmorPiece(inv.getHelmet());
        applyToArmorPiece(inv.getChestplate());
        applyToArmorPiece(inv.getLeggings());
        applyToArmorPiece(inv.getBoots());
        
        // Atualizar inventário
        player.updateInventory();
        
        // Fallback para o método original
        BukkitUtil.enchantItemByType(player, BukkitUtil.EquipmentType.ARMOR, Enchantment.PROTECTION_ENVIRONMENTAL, ordinal());
    }
    
    private void applyToArmorPiece(ItemStack armor) {
        if (armor != null && isArmorMaterial(armor.getType())) {
            // Remover encantamentos existentes
            armor.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
            // Aplicar novo encantamento se não for NONE
            if (this != NONE) {
                armor.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, ordinal());
            }
        }
    }
    
    private boolean isArmorMaterial(Material material) {
        String name = material.name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || 
               name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }
}