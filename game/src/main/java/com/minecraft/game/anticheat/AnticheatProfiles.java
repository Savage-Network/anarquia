package com.minecraft.game.anticheat;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AnticheatProfiles {

    private static final Map<UUID, AnticheatProfile> PROFILES = new ConcurrentHashMap<>();

    private AnticheatProfiles() {}

    public static AnticheatProfile of(Player player) {
        if (player == null) return null;
        return PROFILES.computeIfAbsent(player.getUniqueId(), AnticheatProfile::new);
    }

    public static AnticheatProfile of(UUID id) {
        if (id == null) return null;
        return PROFILES.computeIfAbsent(id, AnticheatProfile::new);
    }

    public static void remove(UUID id) {
        if (id != null) PROFILES.remove(id);
    }

    public static Map<UUID, AnticheatProfile> all() {
        return PROFILES;
    }
}
