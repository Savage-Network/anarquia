package com.minecraft.eventos.game.skit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controla kits temporarios do evento. Port de {@code SkitController}
 * do modulo Eventos do Savage.
 *
 * <p>Os kits sao aplicados via {@code /skit apply <nome>} - nada e
 * persistido alem da sessao atual.
 */
public final class SkitController {

    private final Map<String, ItemStack[]> presets = new LinkedHashMap<>();
    private final Map<java.util.UUID, String> applied = new HashMap<>();

    public SkitController() {
        // Presets-exemplo. Ampliar conforme spec /skit info.
        presets.put("pvp", new ItemStack[]{
                new ItemStack(Material.IRON_SWORD),
                new ItemStack(Material.GOLDEN_APPLE, 8),
                new ItemStack(Material.COOKED_BEEF, 16)
        });
        presets.put("archer", new ItemStack[]{
                new ItemStack(Material.BOW),
                new ItemStack(Material.ARROW, 32),
                new ItemStack(Material.STONE_SWORD)
        });
    }

    public boolean apply(Player player, String name) {
        ItemStack[] items = presets.get(name.toLowerCase());
        if (items == null) return false;

        PlayerInventory inv = player.getInventory();
        inv.clear();
        for (int i = 0; i < items.length && i < inv.getSize(); i++) {
            inv.setItem(i, items[i].clone());
        }
        applied.put(player.getUniqueId(), name.toLowerCase());
        return true;
    }

    public java.util.Set<String> available() {
        return presets.keySet();
    }

    public String currentOf(Player player) {
        return applied.get(player.getUniqueId());
    }

    public void clear() {
        applied.clear();
    }
}
