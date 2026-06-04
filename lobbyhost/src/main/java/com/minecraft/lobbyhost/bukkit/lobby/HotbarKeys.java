package com.minecraft.lobbyhost.bukkit.lobby;

import com.minecraft.lobbyhost.LobbyHostPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

/**
 * Chaves PDC dos itens da hotbar do lobby.
 */
public final class HotbarKeys {

    public static final NamespacedKey SLOT_KEY =
            new NamespacedKey(LobbyHostPlugin.getInstance(), "slot");

    private HotbarKeys() {}

    public static String readSlot(org.bukkit.inventory.ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        return stack.getItemMeta().getPersistentDataContainer()
                .get(SLOT_KEY, PersistentDataType.STRING);
    }
}
