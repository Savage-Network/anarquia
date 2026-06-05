package com.minecraft.game.anticheat.checks;

import com.minecraft.game.anticheat.AnticheatCheck;
import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.anticheat.AnticheatEngine;
import com.minecraft.game.anticheat.AnticheatProfile;
import com.minecraft.game.anticheat.AnticheatProfiles;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BlockCheckListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (AnticheatEngine.isExempt(player)) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        AnticheatProfile profile = AnticheatProfiles.of(player);
        long now = System.currentTimeMillis();
        if (AnticheatConfig.isCheckEnabled(AnticheatCheck.FAST_BREAK)) {
            long minMs = AnticheatConfig.numInt("fast-break.min-break-ms", 80);
            if (profile.lastBreakStartMs > 0 && now - profile.lastBreakStartMs < minMs) {
                AnticheatEngine.flag(player, AnticheatCheck.FAST_BREAK,
                        (now - profile.lastBreakStartMs) + "ms");
            }
        }
        profile.lastBreakStartMs = now;
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
    }
}
