package com.minecraft.core.bukkit.listener.server;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class DamageListener implements Listener {

    private final ImmutableSet<Material> ACCEPTABLE_MATERIALS = Sets.immutableEnumSet(
            Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD,
            Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.WOOD_PICKAXE,
            Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE,
            Material.WOOD_SPADE, Material.STONE_SPADE, Material.IRON_SPADE, Material.GOLD_SPADE, Material.DIAMOND_SPADE);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = getDamager(event), target = (Player) event.getEntity();

            if (player == null || target == null) return;

            ItemStack hand = player.getItemInHand();

            double damage = hand != null && ACCEPTABLE_MATERIALS.contains(hand.getType())
                    ? Math.max(4.5, event.getDamage() - 1) // Aumentamos o dano base mínimo para 3 e reduzimos a subtração
                    : hand != null && hand.getType() == Material.MUSHROOM_SOUP ? 2 // Aumentamos o dano para 2
                    : event.getDamage();

            double currentHealth = target.getHealth() - damage;

            PlayerDamageTargetEvent damageTargetEvent = new PlayerDamageTargetEvent(
                    player, target,
                    (event.getDamager() instanceof Player) ? PlayerDamageTargetEvent.DamageCause.PLAYER : PlayerDamageTargetEvent.DamageCause.PROJECTILE,
                    event.getFinalDamage(),
                    damage,
                    currentHealth,
                    event.isCancelled()
            );

            damageTargetEvent.call();

            event.setCancelled(damageTargetEvent.isCancelled());
            event.setDamage(damageTargetEvent.getDamage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && !event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            Player player = (Player) event.getEntity();

            double damage = event.getDamage(),
                    currentHealth = player.getHealth() - damage;

            PlayerDamageEvent playerDamage = new PlayerDamageEvent(
                    player,
                    event.getCause(),
                    damage,
                    currentHealth,
                    event.getFinalDamage(),
                    event.isCancelled());

            playerDamage.call();

            event.setCancelled(playerDamage.isCancelled());
            event.setDamage(playerDamage.getDamage());
        }
    }

    private Player getDamager(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            return (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        }
        return null;
    }
}