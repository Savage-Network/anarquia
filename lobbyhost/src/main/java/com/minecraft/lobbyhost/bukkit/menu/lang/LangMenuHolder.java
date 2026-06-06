package com.minecraft.lobbyhost.bukkit.menu.lang;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class LangMenuHolder implements InventoryHolder {

    private Inventory inventory;

    public void bind(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
