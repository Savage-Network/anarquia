package com.minecraft.game.anticheat.checks;

import com.minecraft.game.anticheat.AnticheatCheck;
import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.anticheat.AnticheatEngine;
import com.minecraft.game.anticheat.AnticheatLag;
import com.minecraft.game.anticheat.AnticheatMath;
import com.minecraft.game.anticheat.AnticheatProfile;
import com.minecraft.game.anticheat.AnticheatProfiles;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

public final class MovementCheckListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }

        Player player = event.getPlayer();
        if (AnticheatEngine.isExempt(player)) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        AnticheatProfile profile = AnticheatProfiles.of(player);
        profile.incrementMoves();

        double deltaX = to.getX() - from.getX();
        double deltaY = to.getY() - from.getY();
        double deltaZ = to.getZ() - from.getZ();
        double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double totalDelta = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

        boolean flagged = false;

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.BLINK)
                && totalDelta > AnticheatLag.adjustLimit(
                AnticheatConfig.num("blink.max-delta", 9.0), player)) {
            AnticheatEngine.flag(player, AnticheatCheck.BLINK,
                    String.format("Δ %.1f bl", totalDelta), true);
            flagged = true;
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.TIMER)
                && profile.movesThisSecond > AnticheatConfig.numInt("timer.max-moves-per-second", 22)) {
            AnticheatEngine.flag(player, AnticheatCheck.TIMER,
                    profile.movesThisSecond + " mov/s", true);
            flagged = true;
        }

        if (player.isGliding() && AnticheatConfig.isCheckEnabled(AnticheatCheck.ELYTRA)) {
            double limit = AnticheatLag.adjustLimit(AnticheatConfig.num("elytra.max-speed", 1.85), player);
            if (horizontal > limit) {
                AnticheatEngine.flag(player, AnticheatCheck.ELYTRA,
                        String.format("%.2f bl/tick", horizontal), true);
                flagged = true;
            }
            profile.lastLocation = to.clone();
            profile.lastYaw = to.getYaw();
            profile.lastPitch = to.getPitch();
            if (!flagged) AnticheatEngine.markValidPosition(player, to);
            return;
        }

        boolean nearGround = isNearGround(player, to);

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.GROUND_SPOOF)
                && player.isOnGround() && !nearGround && profile.airTicks > 4) {
            AnticheatEngine.flag(player, AnticheatCheck.GROUND_SPOOF,
                    "onGround=true ar=" + profile.airTicks, true);
            flagged = true;
        }

        if (nearGround) {
            profile.airTicks = 0;
            profile.wasOnGround = true;
        } else {
            profile.airTicks++;
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.SPEED)) {
            double limit = nearGround
                    ? AnticheatConfig.num("speed.ground-limit", 0.68)
                    : AnticheatConfig.num("speed.air-limit", 0.58);
            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                limit += AnticheatConfig.num("speed.sprint-bonus", 0.08);
            }
            if (player.isSprinting()) limit += 0.04;
            limit = AnticheatLag.adjustLimit(limit, player);
            if (horizontal > limit) {
                AnticheatEngine.flag(player, AnticheatCheck.SPEED,
                        String.format("%.2f bl/tick (lim %.2f)", horizontal, limit), true);
                flagged = true;
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.NO_SLOW) && AnticheatMath.inSlowBlock(player)) {
            double slowLimit = AnticheatLag.adjustLimit(
                    AnticheatConfig.num("no-slow.max-speed", 0.22), player);
            if (horizontal > slowLimit) {
                AnticheatEngine.flag(player, AnticheatCheck.NO_SLOW,
                        String.format("%.2f em slow block", horizontal), true);
                flagged = true;
            }
        }

        if (!nearGround && AnticheatConfig.isCheckEnabled(AnticheatCheck.FLY)) {
            int maxAir = AnticheatConfig.numInt("fly.air-ticks", 32);
            if (profile.airTicks >= maxAir && player.getFallDistance() < 2.5f && !player.isSwimming()) {
                AnticheatEngine.flag(player, AnticheatCheck.FLY,
                        "Ar: " + profile.airTicks + " ticks", true);
                profile.airTicks = 0;
                flagged = true;
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.HIGH_JUMP) && deltaY > 0) {
            double maxY = AnticheatLag.adjustLimit(
                    AnticheatConfig.num("high-jump.max-delta-y", 0.85), player);
            if (deltaY > maxY && !player.isOnGround() && !profile.wasOnGround) {
                AnticheatEngine.flag(player, AnticheatCheck.HIGH_JUMP,
                        String.format("ΔY %.2f", deltaY), true);
                flagged = true;
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.STEP) && deltaY > 0) {
            double maxStep = AnticheatConfig.num("step.max-step", 1.25);
            if (deltaY > maxStep && deltaY < 2.0 && horizontal < 0.15) {
                AnticheatEngine.flag(player, AnticheatCheck.STEP,
                        String.format("ΔY %.2f sem sprint", deltaY), true);
                flagged = true;
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.STRAFE) && horizontal > 0) {
            float yawDelta = AnticheatMath.rotationDelta(profile.lastYaw, to.getYaw());
            double minSpeed = AnticheatConfig.num("strafe.min-speed", 0.35);
            float maxYaw = (float) AnticheatConfig.num("strafe.max-yaw-delta", 75);
            if (horizontal > minSpeed && yawDelta > maxYaw) {
                AnticheatEngine.flag(player, AnticheatCheck.STRAFE,
                        String.format("yaw %.0f° @ %.2f", yawDelta, horizontal), true);
                flagged = true;
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.JESUS)) {
            Block below = to.clone().subtract(0, 0.2, 0).getBlock();
            Material belowType = below.getType();
            if ((belowType == Material.WATER || belowType == Material.LAVA)
                    && !player.isSwimming() && !player.isInWater() && nearGround
                    && horizontal > 0.08) {
                AnticheatEngine.flag(player, AnticheatCheck.JESUS, belowType.name(), true);
                flagged = true;
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.NO_FALL)) {
            float fall = player.getFallDistance();
            if (profile.lastFallDistance > AnticheatConfig.num("no-fall.min-fall", 4.0)
                    && fall < 0.5f && nearGround) {
                AnticheatEngine.flag(player, AnticheatCheck.NO_FALL,
                        String.format("queda %.1f → 0", profile.lastFallDistance), true);
                flagged = true;
            }
            profile.lastFallDistance = fall;
        }

        if (player.getOpenInventory().getTopInventory().getType()
                != org.bukkit.event.inventory.InventoryType.CRAFTING
                && AnticheatConfig.isCheckEnabled(AnticheatCheck.INVENTORY_MOVE)) {
            double invLimit = AnticheatLag.adjustLimit(
                    AnticheatConfig.num("inventory-move.speed-limit", 0.45), player);
            if (horizontal > invLimit) {
                AnticheatEngine.flag(player, AnticheatCheck.INVENTORY_MOVE,
                        String.format("%.2f bl/tick c/ GUI", horizontal), true);
                flagged = true;
            }
        }

        profile.lastLocation = to.clone();
        profile.lastYaw = to.getYaw();
        profile.lastPitch = to.getPitch();
        profile.wasOnGround = nearGround;

        if (!flagged) {
            AnticheatEngine.markValidPosition(player, to);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        if (AnticheatConfig.isResetVlOnQuit()) {
            AnticheatEngine.clear(event.getPlayer().getUniqueId());
        } else {
            AnticheatProfiles.remove(event.getPlayer().getUniqueId());
        }
    }

    private boolean isNearGround(Player player, Location loc) {
        return AnticheatMath.hasSolidGround(player, loc, 0.35);
    }
}
