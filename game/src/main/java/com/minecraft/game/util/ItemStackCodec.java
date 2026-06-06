package com.minecraft.game.util;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;

/** Serializa inventarios para persistencia no banco. */
public final class ItemStackCodec {

    private ItemStackCodec() {}

    public static String encode(ItemStack[] items) {
        if (items == null || items.length == 0) return "";
        return Base64.getEncoder().encodeToString(ItemStack.serializeItemsAsBytes(items));
    }

    public static ItemStack[] decode(String raw, int size) {
        ItemStack[] result = new ItemStack[size];
        if (raw == null || raw.isBlank()) return result;

        try {
            ItemStack[] loaded = ItemStack.deserializeItemsFromBytes(Base64.getDecoder().decode(raw));
            System.arraycopy(loaded, 0, result, 0, Math.min(loaded.length, size));
        } catch (RuntimeException ignored) {
            // inventario corrompido — trata como vazio
        }
        return result;
    }
}
