package com.minecraft.game.bukkit.listener;

import com.minecraft.game.bukkit.semi.CombatTagManager;
import com.minecraft.game.bukkit.semi.KillRewardService;
import com.minecraft.game.bukkit.semi.SpawnProtection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class SemiAnarchyListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null || attacker.equals(victim)) return;

        if (SpawnProtection.isProtected(victim.getLocation())
                || SpawnProtection.isProtected(attacker.getLocation())) {
            event.setCancelled(true);
            return;
        }

        CombatTagManager.tag(victim);
        CombatTagManager.tag(attacker);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnvironmentalDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (SpawnProtection.isProtected(player.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer != null) {
            KillRewardService.handleKill(killer, victim);
        }
        CombatTagManager.clear(victim);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!CombatTagManager.isTagged(player)) return;

        event.quitMessage(net.kyori.adventure.text.Component.text(
                "§c" + player.getName() + " deslogou em combate e foi eliminado."));

        player.setHealth(0.0);
        CombatTagManager.clear(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (SpawnProtection.isProtected(event.getBlock().getLocation())) {
            if (!event.getPlayer().hasPermission("game.admin")) event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (SpawnProtection.isProtected(event.getBlock().getLocation())) {
            if (!event.getPlayer().hasPermission("game.admin")) event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (SpawnProtection.isProtected(event.getBlock().getLocation())) {
            if (!event.getPlayer().hasPermission("game.admin")) event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (SpawnProtection.isProtected(event.getBlock().getLocation())) {
            if (!event.getPlayer().hasPermission("game.admin")) event.setCancelled(true);
        }
    }

    private static Player resolveAttacker(org.bukkit.entity.Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) return p;
        return null;
    }
}
