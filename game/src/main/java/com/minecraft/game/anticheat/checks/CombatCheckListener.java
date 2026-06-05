package com.minecraft.game.anticheat.checks;

import com.minecraft.game.anticheat.AnticheatCheck;
import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.anticheat.AnticheatEngine;
import com.minecraft.game.anticheat.AnticheatProfile;
import com.minecraft.game.anticheat.AnticheatProfiles;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.util.Vector;

public final class CombatCheckListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (AnticheatEngine.isExempt(attacker)) return;

        Entity victim = event.getEntity();
        AnticheatProfile profile = AnticheatProfiles.of(attacker);
        profile.recordHit(victim);

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.REACH)) {
            double dist = attacker.getLocation().distance(victim.getLocation());
            double eyeDist = attacker.getEyeLocation().distance(victim.getLocation());
            double reach = Math.min(dist, eyeDist);
            double max = AnticheatConfig.num("reach.max-reach", 3.35);
            if (reach > max) {
                AnticheatEngine.flag(attacker, AnticheatCheck.REACH,
                        String.format("%.2f bl (lim %.2f)", reach, max));
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.KILL_AURA)) {
            int maxTargets = AnticheatConfig.numInt("kill-aura.max-targets", 3);
            if (profile.distinctTargetsInWindow() > maxTargets) {
                AnticheatEngine.flag(attacker, AnticheatCheck.KILL_AURA,
                        profile.distinctTargetsInWindow() + " alvos");
                profile.clearTargetWindow();
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.NO_SWING)) {
            long sinceSwing = System.currentTimeMillis() - profile.lastSwingMs;
            if (profile.lastSwingMs > 0 && sinceSwing > 350) {
                AnticheatEngine.flag(attacker, AnticheatCheck.NO_SWING,
                        sinceSwing + "ms sem swing");
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.VELOCITY)
                && victim instanceof Player victimPlayer
                && victimPlayer.getGameMode() == GameMode.SURVIVAL
                && !AnticheatEngine.isExempt(victimPlayer)) {
            double expected = event.getDamage() * 0.08;
            BukkitSchedulerCheck.scheduleVelocity(victimPlayer, expected);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwing(PlayerAnimationEvent event) {
        AnticheatProfiles.of(event.getPlayer()).recordSwing();
    }

    /** Verifica knockback 2 ticks após o hit. */
    static final class BukkitSchedulerCheck {
        private BukkitSchedulerCheck() {}

        static void scheduleVelocity(Player victim, double expected) {
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                    com.minecraft.game.Core.getJavaPlugin(), () -> {
                        if (!victim.isOnline()) return;
                        Vector v = victim.getVelocity();
                        double mag = Math.sqrt(v.getX() * v.getX() + v.getZ() * v.getZ());
                        double ratio = expected <= 0 ? 1.0 : mag / expected;
                        if (ratio < AnticheatConfig.num("velocity.min-ratio", 0.35)) {
                            AnticheatEngine.flag(victim, AnticheatCheck.VELOCITY,
                                    String.format("KB %.0f%%", ratio * 100));
                        }
                    }, 2L);
        }
    }
}
