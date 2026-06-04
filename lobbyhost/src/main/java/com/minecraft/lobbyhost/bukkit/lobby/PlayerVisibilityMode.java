package com.minecraft.lobbyhost.bukkit.lobby;

import org.bukkit.Material;

/**
 * Modos de visibilidade de jogadores no hub (espelho do Savage).
 */
public enum PlayerVisibilityMode {

    ALL("§a§lVisibilidade §8| §fTodos", Material.LIME_DYE),
    FRIENDS_ONLY("§e§lVisibilidade §8| §fAmigos", Material.YELLOW_DYE),
    NONE("§c§lVisibilidade §8| §fNinguem", Material.GRAY_DYE);

    private final String displayName;
    private final Material material;

    PlayerVisibilityMode(String displayName, Material material) {
        this.displayName = displayName;
        this.material = material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public PlayerVisibilityMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
