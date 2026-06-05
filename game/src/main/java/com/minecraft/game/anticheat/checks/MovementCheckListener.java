package com.minecraft.game.anticheat.checks;

import com.minecraft.game.anticheat.AnticheatCheck;
import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.anticheat.AnticheatEngine;
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

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.BLINK)
                && totalDelta > AnticheatConfig.num("blink.max-delta", 10.0)) {
            AnticheatEngine.flag(player, AnticheatCheck.BLINK,
                    String.format("Δ %.1f bl", totalDelta));
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.TIMER)
                && profile.movesThisSecond > AnticheatConfig.numInt("timer.max-moves-per-second", 22)) {
            AnticheatEngine.flag(player, AnticheatCheck.TIMER,
                    profile.movesThisSecond + " mov/s");
        }

        if (player.isGliding() && AnticheatConfig.isCheckEnabled(AnticheatCheck.ELYTRA)) {
            if (horizontal > AnticheatConfig.num("elytra.max-speed", 1.8)) {
                AnticheatEngine.flag(player, AnticheatCheck.ELYTRA,
                        String.format("%.2f bl/tick", horizontal));
            }
            return;
        }

        boolean nearGround = isNearGround(player, to);
        if (nearGround) {
            profile.airTicks = 0;
            profile.wasOnGround = true;
        } else {
            profile.airTicks++;
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.SPEED)) {
            double limit = nearGround
                    ? AnticheatConfig.num("speed.ground-limit", 0.65)
                    : AnticheatConfig.num("speed.air-limit", 0.55);
            if (player.hasPotionEffect(PotionEffectType.SPEED)) limit += 0.12;
            if (horizontal > limit) {
                AnticheatEngine.flag(player, AnticheatCheck.SPEED,
                        String.format("%.2f bl/tick (lim %.2f)", horizontal, limit));
            }
        }

        if (!nearGround && AnticheatConfig.isCheckEnabled(AnticheatCheck.FLY)) {
            int maxAir = AnticheatConfig.numInt("fly.air-ticks", 28);
            if (profile.airTicks >= maxAir && player.getFallDistance() < 2.5f && !player.isSwimming()) {
                AnticheatEngine.flag(player, AnticheatCheck.FLY,
                        "Ar: " + profile.airTicks + " ticks");
                profile.airTicks = 0;
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.HIGH_JUMP) && deltaY > 0) {
            double maxY = AnticheatConfig.num("high-jump.max-delta-y", 0.85);
            if (deltaY > maxY && !player.isOnGround() && !profile.wasOnGround) {
                AnticheatEngine.flag(player, AnticheatCheck.HIGH_JUMP,
                        String.format("ΔY %.2f", deltaY));
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.STEP) && deltaY > 0) {
            double maxStep = AnticheatConfig.num("step.max-step", 1.25);
            if (deltaY > maxStep && deltaY < 2.0 && horizontal < 0.15) {
                AnticheatEngine.flag(player, AnticheatCheck.STEP,
                        String.format("ΔY %.2f sem sprint", deltaY));
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.JESUS)) {
            Block below = to.clone().subtract(0, 0.2, 0).getBlock();
            Material belowType = below.getType();
            if ((belowType == Material.WATER || belowType == Material.LAVA)
                    && !player.isSwimming() && !player.isInWater() && nearGround
                    && horizontal > 0.08) {
                AnticheatEngine.flag(player, AnticheatCheck.JESUS, belowType.name());
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.NO_FALL)) {
            float fall = player.getFallDistance();
            if (profile.lastFallDistance > AnticheatConfig.num("no-fall.min-fall", 4.0)
                    && fall < 0.5f && nearGround) {
                AnticheatEngine.flag(player, AnticheatCheck.NO_FALL,
                        String.format("queda %.1f → 0", profile.lastFallDistance));
            }
            profile.lastFallDistance = fall;
        }

        if (player.getOpenInventory().getTopInventory().getType()
                != org.bukkit.event.inventory.InventoryType.CRAFTING
                && AnticheatConfig.isCheckEnabled(AnticheatCheck.INVENTORY_MOVE)) {
            double invLimit = AnticheatConfig.num("inventory-move.speed-limit", 0.45);
            if (horizontal > invLimit) {
                AnticheatEngine.flag(player, AnticheatCheck.INVENTORY_MOVE,
                        String.format("%.2f bl/tick c/ GUI", horizontal));
            }
        }

        profile.lastLocation = to.clone();
        profile.wasOnGround = nearGround;
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
        if (player.isOnGround() || player.isInWater() || player.isClimbing() || player.isSwimming()) {
            return true;
        }
        Block below = loc.clone().subtract(0, 0.35, 0).getBlock();
        Material type = below.getType();
        return type.isSolid() || type == Material.WATER || type == Material.LAVA;
    }
}
