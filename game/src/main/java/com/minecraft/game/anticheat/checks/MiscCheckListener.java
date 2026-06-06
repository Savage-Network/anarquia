package com.minecraft.game.anticheat.checks;

import com.minecraft.game.anticheat.AnticheatCheck;
import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.anticheat.AnticheatEngine;
import com.minecraft.game.anticheat.AnticheatProfile;
import com.minecraft.game.anticheat.AnticheatProfiles;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class MiscCheckListener implements Listener {

    private static final Map<UUID, AtomicInteger> LEFT_CLICKS = new ConcurrentHashMap<>();
    private static final Map<UUID, AtomicInteger> RIGHT_CLICKS = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND) return;
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            LEFT_CLICKS.computeIfAbsent(id, k -> new AtomicInteger()).incrementAndGet();
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            RIGHT_CLICKS.computeIfAbsent(id, k -> new AtomicInteger()).incrementAndGet();
        }

        if (event.getItem() != null && event.getItem().getType().name().contains("BOW")
                && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            AnticheatProfiles.of(player).lastBowDrawMs = System.currentTimeMillis();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (AnticheatEngine.isExempt(player)) return;
        if (!AnticheatConfig.isCheckEnabled(AnticheatCheck.FAST_BOW)) return;

        AnticheatProfile profile = AnticheatProfiles.of(player);
        long draw = System.currentTimeMillis() - profile.lastBowDrawMs;
        long min = AnticheatConfig.numInt("fast-bow.min-draw-ms", 350);
        if (profile.lastBowDrawMs > 0 && draw < min) {
            AnticheatEngine.flag(player, AnticheatCheck.FAST_BOW, draw + "ms");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (AnticheatEngine.isExempt(player)) return;
        if (!AnticheatConfig.isCheckEnabled(AnticheatCheck.FAST_EAT)) return;

        AnticheatProfile profile = AnticheatProfiles.of(player);
        long elapsed = System.currentTimeMillis() - profile.lastEatStartMs;
        long min = AnticheatConfig.numInt("fast-eat.min-eat-ms", 1400);
        if (profile.lastEatStartMs > 0 && elapsed < min) {
            AnticheatEngine.flag(player, AnticheatCheck.FAST_EAT, elapsed + "ms");
        }
        profile.lastEatStartMs = System.currentTimeMillis();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        LEFT_CLICKS.remove(id);
        RIGHT_CLICKS.remove(id);
    }

    public static void flushCpsWindow() {
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();
            int left = LEFT_CLICKS.getOrDefault(id, new AtomicInteger()).getAndSet(0);
            int right = RIGHT_CLICKS.getOrDefault(id, new AtomicInteger()).getAndSet(0);
            AnticheatProfiles.of(player).setCps(left, right);

            if (AnticheatConfig.isCheckEnabled(AnticheatCheck.AUTO_CLICK) && !AnticheatEngine.isExempt(player)) {
                int max = AnticheatConfig.numInt("auto-click.max-cps", 22);
                int cps = Math.max(left, right);
                if (cps > max) {
                    AnticheatEngine.flag(player, AnticheatCheck.AUTO_CLICK, cps + " CPS");
                }
            }
        }
    }
}
