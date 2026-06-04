package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.gadget.GadgetCollectible;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GadgetOperator extends CollectibleOperator {

    private final Map<Integer, ItemStack> saved = new HashMap<>();

    public GadgetOperator(Player host, GadgetCollectible gadget) {
        super(host, CollectibleCategory.GADGET);
        setCategoriesWithConflict(Arrays.asList(CollectibleCategory.ARTIFACT));
        handle(gadget);
    }

    @Override
    public void handle(Collectible collectible) {
        GadgetCollectible gadget = (GadgetCollectible) collectible;
        Player player = getHost();
        if (player == null)
            return;

        PlayerInventory inv = player.getInventory();
        int slot = gadget.getHotbarSlot();
        saved.put(slot, inv.getItem(slot) == null ? null : inv.getItem(slot).clone());
        inv.setItem(slot, gadget.createItem());
        player.updateInventory();
        setCollectible(gadget);
    }

    @Override
    public void cancel() {
        Player player = getHost();
        if (player == null)
            return;

        PlayerInventory inv = player.getInventory();
        for (Map.Entry<Integer, ItemStack> entry : saved.entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue());
        }
        saved.clear();
        player.updateInventory();
    }
}
