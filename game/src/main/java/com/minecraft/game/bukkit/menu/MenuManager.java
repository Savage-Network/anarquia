package com.minecraft.game.bukkit.menu;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MenuManager {

    private static final Map<UUID, Menu> OPEN = new ConcurrentHashMap<>();

    private MenuManager() {}

    public static void track(Player player, Menu menu) {
        OPEN.put(player.getUniqueId(), menu);
    }

    public static Menu get(Player player) {
        return OPEN.get(player.getUniqueId());
    }

    public static void untrack(Player player) {
        OPEN.remove(player.getUniqueId());
    }
}
