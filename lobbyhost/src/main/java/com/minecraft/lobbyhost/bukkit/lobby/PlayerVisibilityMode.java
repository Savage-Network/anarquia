package com.minecraft.lobbyhost.bukkit.lobby;

import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Material;

/**
 * Modos de visibilidade de jogadores no hub.
 */
public enum PlayerVisibilityMode {

    ALL(ModernText.gradient("Visibilidade", ModernText.SUCCESS, "#86EFAC") + " <#64748B>| <#F1F5F9>Todos",
            Material.LIME_DYE),
    FRIENDS_ONLY(ModernText.gradient("Visibilidade", ModernText.WARN, "#FDE68A") + " <#64748B>| <#F1F5F9>Amigos",
            Material.YELLOW_DYE),
    NONE(ModernText.gradient("Visibilidade", ModernText.ERROR, "#FCA5A5") + " <#64748B>| <#F1F5F9>Ninguém",
            Material.GRAY_DYE);

    private final String displayNameMini;
    private final Material material;

    PlayerVisibilityMode(String displayNameMini, Material material) {
        this.displayNameMini = displayNameMini;
        this.material = material;
    }

    public String getDisplayNameMini() {
        return displayNameMini;
    }

    public Material getMaterial() {
        return material;
    }

    public PlayerVisibilityMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
