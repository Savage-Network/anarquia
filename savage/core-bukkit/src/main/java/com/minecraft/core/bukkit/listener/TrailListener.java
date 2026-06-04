package com.minecraft.core.bukkit.listener;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.type.trail.TrailCollectible;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class TrailListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        // Verificar se o jogador realmente se moveu (não apenas rotação)
        if (from.getBlockX() == to.getBlockX() && 
            from.getBlockY() == to.getBlockY() && 
            from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;
        
        // Verificar se tem rastro ativo
        for (String activeId : account.getActiveCollectibles()) {
            if (activeId.startsWith("trail:")) {
                Collectible collectible = Core.getCollectibleController().of(activeId);
                
                if (collectible instanceof TrailCollectible) {
                    TrailCollectible trail = (TrailCollectible) collectible;
                    trail.applyTrail(player, player.getLocation());
                }
            }
        }
    }
}
