package com.minecraft.game.bukkit.semi;

import org.bukkit.Location;

public final class SpawnProtection {

    private SpawnProtection() {}

    public static boolean isProtected(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        Location spawn = SemiAnarchyConfig.spawnPoint();
        if (!loc.getWorld().equals(spawn.getWorld())) return false;
        return loc.distanceSquared(spawn) <= (double) SemiAnarchyConfig.spawnRadius()
                * SemiAnarchyConfig.spawnRadius();
    }
}
