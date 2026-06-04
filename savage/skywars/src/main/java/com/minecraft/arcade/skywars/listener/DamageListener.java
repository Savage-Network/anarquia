package com.minecraft.arcade.skywars.listener;

import com.minecraft.arcade.skywars.arcade.Arcade;
import com.minecraft.arcade.skywars.arcade.arena.Arena;
import com.minecraft.arcade.skywars.user.User;
import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class DamageListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDamage(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Arena arena = user.getArena();
        if (arena == null) return;

        Arcade arcade = (Arcade) arena.getArcade();

        if (!arcade.hasFeature(ArcadeFeature.DAMAGE)) {
            event.setCancelled(true);
            return;
        }

        if (!user.getContext().isAlive() || !arena.isPhase(RoomPhase.PLAYING)) {
            event.setCancelled(true);
            return;
        }

        if (!event.isCancelled() && event.isDead()) {
            event.setCancelled(true);
            
            // Restaurar vida ANTES de chamar handleDeath para evitar PlayerDeathEvent
            if (player.getHealth() < player.getMaxHealth())
                player.setHealth(player.getMaxHealth());

            User killer = user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null;

            arcade.handleDeath(arena, user, killer, Arcade.DeathCause.PLAYER);
        }
    }

    @EventHandler
    public void onVoid(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Arena arena = user.getArena();
        if (arena == null) return;
        
        if (!arena.isPhase(RoomPhase.PLAYING) || !user.getContext().isAlive()) {
            return;
        }
        
        Arcade arcade = (Arcade) arena.getArcade();

        Location to = event.getTo();
        Location spawn = arena.getLocation("spawn");

        if (spawn == null) return;

        if (to.getY() <= spawn.getY() - 80) {
            // Teleportar jogador para cima para evitar cair infinitamente
            player.teleport(player.getLocation().clone().add(0, spawn.getY() + 35, 0));
            
            // Restaurar vida para evitar PlayerDeathEvent
            player.setHealth(player.getMaxHealth());
            
            User killer = user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null;
            arcade.handleDeath(arena, user, killer, Arcade.DeathCause.VOID);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamageTarget(PlayerDamageTargetEvent event) {
        Player target = event.getTarget(), damager = event.getPlayer();

        User user = (User) User.of(target.getUniqueId()), killer = (User) User.of(damager.getUniqueId());

        if (user == null || killer == null) {
            event.setCancelled(true);
            return;
        }

        Arena arena = user.getArena();
        if (arena == null) {
            event.setCancelled(true);
            return;
        }

        Arcade arcade = (Arcade) arena.getArcade();

        if (damager.equals(target)) {
            event.setCancelled(true);
            return;
        }

        if (!arena.isPhase(RoomPhase.PLAYING) || !user.getContext().isAlive() || !killer.getContext().isAlive()) {
            event.setCancelled(true);
            return;
        }

        if (!arcade.hasFeature(ArcadeFeature.DAMAGE) || !arcade.equals(killer.getArena().getArcade())) {
            event.setCancelled(true);
            return;
        }

        if (!event.isCancelled()) {
            if (event.isDead()) {
                event.setCancelled(true);

                arcade.handleDeath(arena, user, killer, Arcade.DeathCause.PLAYER);
            } else {
                user.setCombat(damager);
                killer.setCombat(target);
            }
        }
    }
}

