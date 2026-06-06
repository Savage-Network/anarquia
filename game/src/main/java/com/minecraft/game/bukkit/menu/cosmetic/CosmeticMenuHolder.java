package com.minecraft.game.bukkit.menu.cosmetic;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class CosmeticMenuHolder implements InventoryHolder {

    private final String menuId;
    private final String backTarget;
    private Inventory inventory;

    public CosmeticMenuHolder(String menuId) {
        this(menuId, "cosmetics_hub");
    }

    public CosmeticMenuHolder(String menuId, String backTarget) {
        this.menuId = menuId;
        this.backTarget = backTarget != null ? backTarget : "cosmetics_hub";
    }

    public String getMenuId() {
        return menuId;
    }

    public String getBackTarget() {
        return backTarget;
    }

    void bind(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
