package com.minecraft.game.anticheat.checks;

import com.minecraft.game.anticheat.AnticheatCheck;
import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.anticheat.AnticheatEngine;
import com.minecraft.game.anticheat.AnticheatLag;
import com.minecraft.game.anticheat.AnticheatMath;
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
        profile.recordAttack();

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.MULTI_HIT)) {
            int maxHits = AnticheatConfig.numInt("multi-hit.max-hits", 2);
            if (profile.hitsThisTick > maxHits) {
                AnticheatEngine.flag(attacker, AnticheatCheck.MULTI_HIT,
                        profile.hitsThisTick + " hits/tick");
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.REACH)) {
            double dist = attacker.getLocation().distance(victim.getLocation());
            double eyeDist = attacker.getEyeLocation().distance(victim.getLocation());
            double reach = Math.min(dist, eyeDist);
            double max = AnticheatLag.adjustLimit(AnticheatConfig.num("reach.max-reach", 3.35), attacker);
            if (reach > max) {
                AnticheatEngine.flag(attacker, AnticheatCheck.REACH,
                        String.format("%.2f bl (lim %.2f)", reach, max));
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.HITBOX)) {
            double reach = AnticheatMath.hitboxReach(attacker, victim);
            double max = AnticheatLag.adjustLimit(AnticheatConfig.num("hitbox.max-reach", 3.05), attacker);
            if (reach > max) {
                AnticheatEngine.flag(attacker, AnticheatCheck.HITBOX,
                        String.format("AABB %.2f (lim %.2f)", reach, max));
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.WALL_HIT)
                && AnticheatMath.hasBlockObstruction(attacker, victim)) {
            AnticheatEngine.flag(attacker, AnticheatCheck.WALL_HIT,
                    "LOS bloqueada → " + victim.getName());
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
            long maxMs = AnticheatConfig.numInt("no-swing.max-ms", 350);
            if (profile.lastSwingMs > 0 && sinceSwing > maxMs) {
                AnticheatEngine.flag(attacker, AnticheatCheck.NO_SWING,
                        sinceSwing + "ms sem swing");
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.CRITICALS)) {
            if (isImpossibleCritical(attacker, event)) {
                AnticheatEngine.flag(attacker, AnticheatCheck.CRITICALS,
                        "crit sem queda (FD=" + String.format("%.1f", attacker.getFallDistance()) + ")");
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.AIM_ASSIST)) {
            float rotDelta = AnticheatMath.totalRotationDelta(
                    profile.lastYaw, profile.lastPitch,
                    attacker.getLocation().getYaw(), attacker.getLocation().getPitch());
            float maxRot = (float) AnticheatConfig.num("aim-assist.max-rotation", 45);
            if (rotDelta > maxRot && profile.lastSwingMs > 0
                    && System.currentTimeMillis() - profile.lastSwingMs < 120) {
                AnticheatEngine.flag(attacker, AnticheatCheck.AIM_ASSIST,
                        String.format("snap %.0f°", rotDelta));
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.VELOCITY)
                && victim instanceof Player victimPlayer
                && victimPlayer.getGameMode() == GameMode.SURVIVAL
                && !AnticheatEngine.isExempt(victimPlayer)) {
            double expected = event.getDamage() * 0.08;
            scheduleVelocity(victimPlayer, expected);
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.MULTI_ACTION)
                && profile.hasMultiActionConflict()) {
            AnticheatEngine.flag(attacker, AnticheatCheck.MULTI_ACTION, "hit + outra ação");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwing(PlayerAnimationEvent event) {
        AnticheatProfiles.of(event.getPlayer()).recordSwing();
    }

    private static boolean isImpossibleCritical(Player attacker, EntityDamageByEntityEvent event) {
        if (attacker.isOnGround() && attacker.getFallDistance() <= 0.001f) {
            return event.getDamage() > 1.0 && !attacker.isInsideVehicle()
                    && !attacker.isClimbing() && !attacker.isInWater();
        }
        return false;
    }

    private static void scheduleVelocity(Player victim, double expected) {
        int delay = AnticheatConfig.numInt("velocity.delay-ticks", 3);
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
                }, delay);
    }
}
