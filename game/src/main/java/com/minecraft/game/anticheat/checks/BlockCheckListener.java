package com.minecraft.game.anticheat.checks;

import com.minecraft.game.anticheat.AnticheatCheck;
import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.anticheat.AnticheatEngine;
import com.minecraft.game.anticheat.AnticheatProfile;
import com.minecraft.game.anticheat.AnticheatProfiles;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class BlockCheckListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (AnticheatEngine.isExempt(player)) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        AnticheatProfile profile = AnticheatProfiles.of(player);
        profile.incrementBreaks();
        long now = System.currentTimeMillis();

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.FAST_BREAK)) {
            long minMs = AnticheatConfig.numInt("fast-break.min-break-ms", 80);
            if (profile.lastBreakStartMs > 0 && now - profile.lastBreakStartMs < minMs) {
                AnticheatEngine.flag(player, AnticheatCheck.FAST_BREAK,
                        (now - profile.lastBreakStartMs) + "ms");
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.NUKER)) {
            int max = AnticheatConfig.numInt("nuker.max-per-second", 18);
            if (profile.breaksThisSecond > max) {
                AnticheatEngine.flag(player, AnticheatCheck.NUKER,
                        profile.breaksThisSecond + " bl/s");
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.GHOST_HAND)) {
            double dist = player.getEyeLocation().distance(event.getBlock().getLocation().add(0.5, 0.5, 0.5));
            double max = AnticheatConfig.num("ghost-hand.max-reach", 6.0);
            if (dist > max) {
                AnticheatEngine.flag(player, AnticheatCheck.GHOST_HAND,
                        String.format("break %.1f bl", dist));
            }
        }

        profile.lastBreakStartMs = now;

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.MULTI_ACTION)
                && profile.hasMultiActionConflict()) {
            AnticheatEngine.flag(player, AnticheatCheck.MULTI_ACTION, "break + outra ação");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (AnticheatEngine.isExempt(player)) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        AnticheatProfile profile = AnticheatProfiles.of(player);
        profile.incrementPlaces();

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.FAST_PLACE)) {
            int max = AnticheatConfig.numInt("fast-place.max-per-second", 14);
            if (profile.placesThisSecond > max) {
                AnticheatEngine.flag(player, AnticheatCheck.FAST_PLACE,
                        profile.placesThisSecond + "/s");
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.SCAFFOLD)) {
            Block placed = event.getBlockAgainst();
            Material type = placed.getType();
            if (!player.isOnGround() && player.getFallDistance() > 1.5f
                    && type.isSolid() && player.getLocation().getPitch() > 65f) {
                AnticheatEngine.flag(player, AnticheatCheck.SCAFFOLD,
                        "pitch " + (int) player.getLocation().getPitch());
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.AIR_PLACE)) {
            Block block = event.getBlock();
            if (!hasAdjacentSupport(block)) {
                AnticheatEngine.flag(player, AnticheatCheck.AIR_PLACE,
                        block.getType().name() + " sem suporte");
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.GHOST_HAND)) {
            double dist = player.getEyeLocation().distance(event.getBlock().getLocation().add(0.5, 0.5, 0.5));
            double max = AnticheatConfig.num("ghost-hand.max-reach", 6.0);
            if (dist > max) {
                AnticheatEngine.flag(player, AnticheatCheck.GHOST_HAND,
                        String.format("place %.1f bl", dist));
            }
        }

        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.MULTI_ACTION)
                && profile.hasMultiActionConflict()) {
            AnticheatEngine.flag(player, AnticheatCheck.MULTI_ACTION, "place + outra ação");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Player player = event.getPlayer();
        if (AnticheatEngine.isExempt(player)) return;
        if (!AnticheatConfig.isCheckEnabled(AnticheatCheck.GHOST_HAND)) return;

        double dist = player.getEyeLocation().distance(
                event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5));
        double max = AnticheatConfig.num("ghost-hand.max-reach", 6.0);
        if (dist > max) {
            AnticheatEngine.flag(player, AnticheatCheck.GHOST_HAND,
                    String.format("interact %.1f bl", dist));
        }
    }

    private static boolean hasAdjacentSupport(Block block) {
        for (BlockFace face : BlockFace.values()) {
            if (face == BlockFace.SELF) continue;
            Block rel = block.getRelative(face);
            if (rel.getType().isSolid()) return true;
        }
        return false;
    }
}
