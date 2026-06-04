package com.minecraft.arcade.thebridge.listener;

import com.minecraft.arcade.thebridge.arcade.arena.Arena;
import com.minecraft.arcade.thebridge.user.User;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer()) return;

        Arena arena = user.getArena();

        // Bloquear colocação de blocos durante WAITING e STARTING
        if (arena.isPhase(RoomPhase.WAITING) || arena.isPhase(RoomPhase.STARTING)) {
            event.setCancelled(true);
            return;
        }

        // Durante PLAYING, verificar se está dentro dos limites da ponte
        if (arena.isPhase(RoomPhase.PLAYING)) {
            if (!user.getArcade().hasFeature(com.minecraft.core.arcade.feature.ArcadeFeature.BUILD)) {
                event.setCancelled(true);
                return;
            }
            
            // Verificar se o bloco está entre ponte-1 e ponte-2
            if (!isWithinBridgeLimits(arena, event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer()) return;

        Arena arena = user.getArena();

        // Bloquear quebra de blocos durante WAITING e STARTING
        if (arena.isPhase(RoomPhase.WAITING) || arena.isPhase(RoomPhase.STARTING)) {
            event.setCancelled(true);
            return;
        }

        // Durante PLAYING, verificar se está dentro dos limites da ponte
        if (arena.isPhase(RoomPhase.PLAYING)) {
            if (!user.getArcade().hasFeature(com.minecraft.core.arcade.feature.ArcadeFeature.BUILD)) {
                event.setCancelled(true);
                return;
            }
            
            // Verificar se o bloco está entre ponte-1 e ponte-2
            if (!isWithinBridgeLimits(arena, event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }
    
    /**
     * Verifica se uma localização está dentro dos limites da ponte (entre ponte-1 e ponte-2)
     */
    private boolean isWithinBridgeLimits(Arena arena, org.bukkit.Location location) {
        org.bukkit.Location ponte1 = arena.getLocation("ponte_1");
        org.bukkit.Location ponte2 = arena.getLocation("ponte_2");
        
        if (ponte1 == null || ponte2 == null) {
            // Se não tiver limites configurados, permitir em qualquer lugar
            return true;
        }
        
        // Criar uma região cúbica entre ponte-1 e ponte-2
        double minX = Math.min(ponte1.getX(), ponte2.getX());
        double maxX = Math.max(ponte1.getX(), ponte2.getX());
        double minY = Math.min(ponte1.getY(), ponte2.getY());
        double maxY = Math.max(ponte1.getY(), ponte2.getY());
        double minZ = Math.min(ponte1.getZ(), ponte2.getZ());
        double maxZ = Math.max(ponte1.getZ(), ponte2.getZ());
        
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }
}
