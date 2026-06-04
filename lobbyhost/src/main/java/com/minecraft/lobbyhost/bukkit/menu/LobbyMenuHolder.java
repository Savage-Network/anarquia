package com.minecraft.lobbyhost.bukkit.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Marca inventarios GUI do lobbyhost para o {@link LobbyMenuListener}.
 */
public final class LobbyMenuHolder implements InventoryHolder {

    private final String menuId;
    private Inventory inventory;

    public LobbyMenuHolder(String menuId) {
        this.menuId = menuId;
    }

    public String getMenuId() {
        return menuId;
    }

    void bind(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
