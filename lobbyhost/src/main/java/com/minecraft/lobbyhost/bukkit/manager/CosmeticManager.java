package com.minecraft.lobbyhost.bukkit.manager;

import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.bukkit.cosmetic.ParticleRunner;
import org.bukkit.scheduler.BukkitTask;

public final class CosmeticManager {

    private static BukkitTask particleTask;

    private CosmeticManager() {}

    public static void init() {
        if (particleTask != null) {
            particleTask.cancel();
        }
        particleTask = new ParticleRunner().runTaskTimer(LobbyHostPlugin.getInstance(), 4L, 4L);
    }

    public static void shutdown() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
    }
}
