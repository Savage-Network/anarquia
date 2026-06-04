package com.minecraft.hungergames.listener.player;

import com.minecraft.hungergames.Hungergames;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener de jogador para renderizar scoreboard ao entrar.
 */
public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Hungergames hg = Hungergames.getInstance();
        if (hg == null || hg.getScoreboardController() == null) return;

        hg.getScoreboardController().render(event.getPlayer(), hg.getCurrentStage());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Hungergames hg = Hungergames.getInstance();
        if (hg != null && hg.getScoreboardController() != null) {
            hg.getScoreboardController().cancelUpdate(event.getPlayer().getUniqueId());
        }
    }
}
