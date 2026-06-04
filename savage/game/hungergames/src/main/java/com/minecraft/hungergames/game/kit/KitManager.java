package com.minecraft.hungergames.game.kit;

import org.bukkit.entity.Player;

import java.util.*;


public class KitManager {

    private final Map<String, Kit> kits = new HashMap<>();
    // Kits selecionados por jogador (suporta 2 kits por jogador)
    private final Map<UUID, List<Kit>> playerKits = new HashMap<>();

    public void register(Kit kit) {
        if (kit == null) return;
        kits.put(kit.getName().toLowerCase(), kit);
        kit.register();
    }

    public void unregisterAll() {
        for (Kit kit : kits.values()) kit.unregister();
        kits.clear();
        playerKits.clear();
    }

    public Kit get(String name) {
        if (name == null) return null;
        return kits.get(name.toLowerCase());
    }

    public Collection<Kit> all() {
        return Collections.unmodifiableCollection(kits.values());
    }

    public void give(Player player, String kitName) {
        Kit kit = get(kitName);
        if (kit != null) {
            kit.onSelect(player);
            setKit(player.getUniqueId(), kit);
        }
    }

    public void clearKits(UUID id) {
        playerKits.remove(id);
    }

    public void setKit(UUID id, Kit kit) {
        if (kit == null || id == null) return;
        List<Kit> kitsList = playerKits.computeIfAbsent(id, k -> new ArrayList<>());

        kitsList.removeIf(existing -> existing.getName().equalsIgnoreCase(kit.getName()));

        if (kitsList.size() >= 2) {
            kitsList.remove(0);
        }
        kitsList.add(kit);
    }

    public List<Kit> getPlayerKits(Player player) {
        if (player == null) return Collections.emptyList();
        return playerKits.getOrDefault(player.getUniqueId(), Collections.emptyList());
    }

    public Kit getPrimaryKit(Player player) {
        List<Kit> list = getPlayerKits(player);
        return list.isEmpty() ? null : list.get(0);
    }

    public Kit getSecondaryKit(Player player) {
        List<Kit> list = getPlayerKits(player);
        return list.size() >= 2 ? list.get(1) : null;
    }
    
}