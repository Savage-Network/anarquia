package com.minecraft.game.bukkit.menu.credits;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class CreditosMenuHolder implements InventoryHolder {

    private Inventory inventory;

    public void bind(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
