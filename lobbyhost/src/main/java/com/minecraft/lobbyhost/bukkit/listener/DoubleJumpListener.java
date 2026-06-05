package com.minecraft.lobbyhost.bukkit.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Enables a simple double‑jump mechanic that is disabled when player is in fly mode.
 *
 * <p>When the player is on the ground we enable flight so that the next
 * press of the jump key (which triggers {@link PlayerToggleFlightEvent})
 * will act as a double jump. After the double jump we disable flight
 * again until the player lands.</p>
 * 
 * <p>If player has fly mode activated via command, double jump is disabled.</p>
 */
public final class DoubleJumpListener implements Listener {
    
    // Track players who are in actual fly mode (via /fly command)
    private static final Set<UUID> playersInFlyMode = new HashSet<>();
    
    /**
     * Marks a player as being in fly mode (called from FlyCommand)
     */
    public static void setFlyMode(Player player, boolean enabled) {
        if (enabled) {
            playersInFlyMode.add(player.getUniqueId());
            // Remove double jump ability when entering fly mode
            if (player.getAllowFlight() && !player.isFlying()) {
                // Only remove if they're not actually flying yet
                // This preserves the fly mode activation
            }
        } else {
            playersInFlyMode.remove(player.getUniqueId());
            // Restore double jump when leaving fly mode if on ground
            if (player.isOnGround()) {
                player.setAllowFlight(true);
            }
        }
    }
    
    /**
     * Check if player is in fly mode (not just double jump mode)
     */
    public static boolean isInFlyMode(Player player) {
        return playersInFlyMode.contains(player.getUniqueId());
    }
    
    /**
     * Clean up when player leaves
     */
    public static void cleanup(Player player) {
        playersInFlyMode.remove(player.getUniqueId());
    }

    /**
     * Double jump desativado.
     */
    @EventHandler
    public void onGroundLanding(PlayerMoveEvent event) {
        // Double jump desativado
    }

    /**
     * Double jump desativado.
     */
    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        
        // Se o jogador está em fly mode, deixa o voo normal funcionar
        if (isInFlyMode(player)) {
            return; // Deixa o evento normal acontecer
        }
        
        // Double jump desativado - cancela tentativa de voo
        if (event.isFlying()) {
            event.setCancelled(true);
        }
    }
}
