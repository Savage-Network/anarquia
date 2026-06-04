package com.minecraft.arcade.thebridge.listener;

import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import com.minecraft.arcade.thebridge.arcade.Arcade;
import com.minecraft.arcade.thebridge.arcade.arena.Arena;
import com.minecraft.arcade.thebridge.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamage(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Arena arena = user.getArena();

        Arcade arcade = user.getArcade();

        // Bloquear dano durante WAITING e STARTING
        if (arena.isPhase(RoomPhase.WAITING) || arena.isPhase(RoomPhase.STARTING)) {
            event.setCancelled(true);
            return;
        }

        if (arena.isPhase(RoomPhase.PLAYING)) {
            if (!arcade.hasFeature(ArcadeFeature.DAMAGE)) {
                event.setCancelled(true);
                return;
            }

            if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL) && !arcade.hasFeature(ArcadeFeature.FALL_DAMAGE)) {
                event.setCancelled(true);
                return;
            }
        }

        event.setCancelled(user.isProtected());

        if (!event.isCancelled() && event.isDead()) {
            event.setCancelled(true);

            User killer = user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null;

            arcade.handleDeath(user, killer, Arcade.DeathCause.PLAYER);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamageTarget(PlayerDamageTargetEvent event) {
        Player player = event.getPlayer(), target = event.getTarget();

        User user = (User) User.of(target.getUniqueId()), killer = (User) User.of(player.getUniqueId());

        if (user == null || killer == null) {
            event.setCancelled(true);
            return;
        }

        if (player.equals(target)) {
            event.setCancelled(true);
            return;
        }

        Arcade arcade = user.getArcade();
        
        Arena arena = user.getArena();

        // Bloquear hit durante WAITING e STARTING
        if (arena.isPhase(RoomPhase.WAITING) || arena.isPhase(RoomPhase.STARTING)) {
            event.setCancelled(true);
            return;
        }

        if (killer.isProtected()) {
            event.setCancelled(true);
            return;
        }

        if (!arcade.hasFeature(ArcadeFeature.DAMAGE) || !arcade.equals(killer.getArcade())) {
            event.setCancelled(true);
            return;
        }

        if (!event.isCancelled()) {

            if (arcade.hasFeature(ArcadeFeature.NOT_TAKE_LIFE)) {
                event.setDamage(0);
                return;
            }

            if (event.isDead()) {
                event.setCancelled(true);

                arcade.handleDeath(user, killer, Arcade.DeathCause.PLAYER);
            } else
                user.setCombat(player);
        }
    }
}
