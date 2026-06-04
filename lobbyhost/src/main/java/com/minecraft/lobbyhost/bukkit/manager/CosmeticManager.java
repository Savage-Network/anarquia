package com.minecraft.lobbyhost.bukkit.manager;

import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.bukkit.cosmetic.CapeRenderer;
import com.minecraft.lobbyhost.bukkit.cosmetic.GadgetHandler;
import com.minecraft.lobbyhost.bukkit.cosmetic.ParticleRunner;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

public final class CosmeticManager {

    @Getter private static final CapeRenderer capeRenderer = new CapeRenderer();
    @Getter private static final GadgetHandler gadgetHandler = new GadgetHandler();
    
    private static BukkitTask particleTask;

    private CosmeticManager() {}

    public static void init() {
        capeRenderer.loadImages();
        
        // Inicia a task de partículas (roda a cada 2 ticks para suavidade)
        if (particleTask != null) {
            particleTask.cancel();
        }
        particleTask = new ParticleRunner().runTaskTimer(LobbyHostPlugin.getInstance(), 2L, 2L);
    }

    public static void shutdown() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
        capeRenderer.removeAll();
        gadgetHandler.shutdown();
    }
}
