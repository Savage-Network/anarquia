package com.minecraft.game.bukkit.listener;

import com.minecraft.game.GamePlugin;
import com.minecraft.game.message.Messages;
import com.minecraft.game.bukkit.semi.CombatTagManager;
import com.minecraft.game.bukkit.semi.KillRewardService;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import com.minecraft.game.bukkit.semi.SpawnProtection;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SemiAnarchyListener implements Listener {

    private static final Map<UUID, Long> LAST_SPAWN_WARN = new ConcurrentHashMap<>();
    private static final long WARN_COOLDOWN_MS = 2500L;

    private static void warn(Player player, String path) {
        long now = System.currentTimeMillis();
        Long last = LAST_SPAWN_WARN.get(player.getUniqueId());
        if (last != null && now - last < WARN_COOLDOWN_MS) return;
        LAST_SPAWN_WARN.put(player.getUniqueId(), now);
        player.sendMessage(Messages.prefixed(path, player));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null || attacker.equals(victim)) return;

        if (SpawnProtection.isProtected(victim.getLocation())
                || SpawnProtection.isProtected(attacker.getLocation())) {
            event.setCancelled(true);
            warn(attacker, "spawn.pvp-blocked");
        }
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
            dropHead(victim);
        }
        CombatTagManager.clear(victim);
        try {
            Location spawn = SemiAnarchyConfig.spawnPoint();
            victim.setRespawnLocation(spawn, true);
        } catch (IllegalStateException ignored) {
            // spawn indisponivel — respawn vanilla
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        try {
            Location spawn = SemiAnarchyConfig.spawnPoint();
            spawn.getChunk().load(true);
            event.setRespawnLocation(spawn);
        } catch (IllegalStateException ignored) {
            return;
        }

        GamePlugin plugin = GamePlugin.getInstance();
        if (plugin == null) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Player player = event.getPlayer();
            if (!player.isOnline()) return;
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.1f);
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0),
                    20, 0.3, 0.4, 0.3, 0.03);
        });
    }

    /** Dropa a cabeça do jogador morto com a skin dele. */
    private static void dropHead(Player victim) {
        org.bukkit.inventory.ItemStack skull =
                new org.bukkit.inventory.ItemStack(org.bukkit.Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta meta =
                (org.bukkit.inventory.meta.SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(victim);
        meta.displayName(net.kyori.adventure.text.Component.text(
                "§7Cabeça de §f" + victim.getName())
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        skull.setItemMeta(meta);
        victim.getWorld().dropItemNaturally(victim.getLocation(), skull);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (SpawnProtection.isProtected(event.getBlock().getLocation())) {
            if (!event.getPlayer().hasPermission("game.admin")) {
                event.setCancelled(true);
                warn(event.getPlayer(), "spawn.build-blocked");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (SpawnProtection.isProtected(event.getBlock().getLocation())) {
            if (!event.getPlayer().hasPermission("game.admin")) {
                event.setCancelled(true);
                warn(event.getPlayer(), "spawn.build-blocked");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (SpawnProtection.isProtected(event.getBlock().getLocation())) {
            if (!event.getPlayer().hasPermission("game.admin")) {
                event.setCancelled(true);
                warn(event.getPlayer(), "spawn.bucket-blocked");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (SpawnProtection.isProtected(event.getBlock().getLocation())) {
            if (!event.getPlayer().hasPermission("game.admin")) {
                event.setCancelled(true);
                warn(event.getPlayer(), "spawn.bucket-blocked");
            }
        }
    }

    private static Player resolveAttacker(org.bukkit.entity.Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) return p;
        return null;
    }
}
