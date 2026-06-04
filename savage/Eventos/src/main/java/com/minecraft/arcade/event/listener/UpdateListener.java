package com.minecraft.arcade.event.listener;

import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateListener {

    public UpdateListener() {
        // Iniciar task de atualização
        new BukkitRunnable() {
            @Override
            public void run() {
                Game game = Event.getGame();
                if (game != null) {
                    game.handlePulse();
                }
            }
        }.runTaskTimer(org.bukkit.plugin.java.JavaPlugin.getPlugin(com.minecraft.arcade.event.Event.class), 20L, 20L);
    }
}
