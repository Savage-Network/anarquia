package com.minecraft.lobbyhost.display.npc;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

/**
 * Cabecas de jogador para {@link LobbyNpc} (ItemDisplay).
 */
public final class SkullProfiles {

    private SkullProfiles() {}

    public static ItemStack createHead(String playerName) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static ItemStack createHeadFromTexture(String textureValue, String signature) {
        if (textureValue == null || textureValue.isBlank()) {
            return createHead("Steve");
        }
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        if (meta == null) return stack;

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "NPC");
        profile.setProperty(new ProfileProperty("textures", textureValue,
                signature == null ? "" : signature));
        meta.setPlayerProfile(profile);
        stack.setItemMeta(meta);
        return stack;
    }
}
