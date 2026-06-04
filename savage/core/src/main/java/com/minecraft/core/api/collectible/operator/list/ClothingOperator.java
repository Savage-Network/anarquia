package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.clothing.ClothingCollectible;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClothingOperator extends CollectibleOperator {

    private final Map<Integer, ItemStack> saved = new HashMap<>();

    public ClothingOperator(Player host, ClothingCollectible clothing) {
        super(host, CollectibleCategory.CLOTHING);
        setCategoriesWithConflict(Arrays.asList(CollectibleCategory.HAT));
        handle(clothing);
    }

    @Override
    public void handle(Collectible collectible) {
        ClothingCollectible clothing = (ClothingCollectible) collectible;
        Player player = getHost();
        if (player == null)
            return;

        PlayerInventory inv = player.getInventory();
        saveSlot(inv, 0, inv.getHelmet());
        saveSlot(inv, 1, inv.getChestplate());
        saveSlot(inv, 2, inv.getLeggings());
        saveSlot(inv, 3, inv.getBoots());

        if (clothing.isHelmet())
            inv.setHelmet(clothing.piece(Material.LEATHER_HELMET));
        if (clothing.isChest())
            inv.setChestplate(clothing.piece(Material.LEATHER_CHESTPLATE));
        if (clothing.isLegs())
            inv.setLeggings(clothing.piece(Material.LEATHER_LEGGINGS));
        if (clothing.isBoots())
            inv.setBoots(clothing.piece(Material.LEATHER_BOOTS));

        player.updateInventory();
        setCollectible(clothing);
    }

    private void saveSlot(PlayerInventory inv, int index, ItemStack stack) {
        saved.put(index, stack == null ? null : stack.clone());
    }

    @Override
    public void cancel() {
        Player player = getHost();
        if (player == null)
            return;

        PlayerInventory inv = player.getInventory();
        inv.setHelmet(saved.getOrDefault(0, null));
        inv.setChestplate(saved.getOrDefault(1, null));
        inv.setLeggings(saved.getOrDefault(2, null));
        inv.setBoots(saved.getOrDefault(3, null));
        saved.clear();
        player.updateInventory();
    }
}
