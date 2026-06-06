package com.minecraft.game.bukkit.listener;

import com.minecraft.game.GameSettings;
import com.minecraft.game.bukkit.semi.CombatLogService;
import com.minecraft.game.bukkit.semi.CombatTagManager;
import com.minecraft.game.bukkit.semi.SpawnProtection;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Combat tag PvP, bloqueios durante combate e punicao por combat log.
 */
public final class CombatLogListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPvpDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null || attacker.equals(victim)) return;

        if (SpawnProtection.isProtected(victim.getLocation())
                || SpawnProtection.isProtected(attacker.getLocation())) {
            return;
        }

        if (attacker.hasPermission("game.combat.bypass")) return;

        CombatTagManager.tagPair(victim, attacker);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!CombatTagManager.isTagged(player)) return;
        if (player.hasPermission("game.combat.bypass")) {
            CombatTagManager.clear(player);
            return;
        }

        event.quitMessage(null);
        CombatLogService.punish(player);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (!CombatTagManager.isTagged(player)) return;
        if (player.hasPermission("game.combat.bypass")) {
            CombatTagManager.clear(player);
            return;
        }

        CombatLogService.punish(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (!GameSettings.COMBAT_BLOCK_TELEPORTS) return;

        Player player = event.getPlayer();
        if (!CombatTagManager.isTagged(player)) return;
        if (player.hasPermission("game.combat.bypass")) return;

        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause == PlayerTeleportEvent.TeleportCause.UNKNOWN) return;

        event.setCancelled(true);
        CombatLogService.denyTeleport(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPearl(PlayerInteractEvent event) {
        if (!GameSettings.COMBAT_BLOCK_PEARLS) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getItem() == null || event.getItem().getType() != Material.ENDER_PEARL) return;

        Player player = event.getPlayer();
        if (!CombatTagManager.isTagged(player)) return;
        if (player.hasPermission("game.combat.bypass")) return;

        event.setCancelled(true);
        CombatLogService.denyPearl(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChorus(PlayerItemConsumeEvent event) {
        if (!GameSettings.COMBAT_BLOCK_CHORUS) return;
        if (event.getItem().getType() != Material.CHORUS_FRUIT) return;

        Player player = event.getPlayer();
        if (!CombatTagManager.isTagged(player)) return;
        if (player.hasPermission("game.combat.bypass")) return;

        event.setCancelled(true);
        CombatLogService.denyChorus(player);
    }

    private static Player resolveAttacker(org.bukkit.entity.Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) return p;
        return null;
    }
}
