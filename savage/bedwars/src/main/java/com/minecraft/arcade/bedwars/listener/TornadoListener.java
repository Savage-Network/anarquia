package com.minecraft.arcade.bedwars.listener;

import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.route.state.ArcadeState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TornadoListener implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_TIME = 10000;
    private static final double TORNADO_RADIUS = 6.0;
    private static final double TORNADO_HEIGHT = 8.0;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.HOPPER) {
            return;
        }

        if (item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) {
            return;
        }

        if (!item.getItemMeta().getDisplayName().equals("§6Tornado")) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.inState(ArcadeState.ALIVE)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (cooldowns.containsKey(playerId)) {
            long timeLeft = (cooldowns.get(playerId) + COOLDOWN_TIME - currentTime) / 1000;
            if (timeLeft > 0) {
                player.sendMessage("§cAguarde §f" + timeLeft + "s §cpara usar o §6Tornado §cnovamente!");
                return;
            }
        }

        activateTornado(player, user);

        cooldowns.put(playerId, currentTime);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }
        player.updateInventory();
    }

    private void activateTornado(Player player, User user) {
        Location center = player.getLocation();

        player.getWorld().playSound(center, Sound.ENDERDRAGON_WINGS, 2.5f, 0.4f);
        player.getWorld().playSound(center, Sound.AMBIENCE_THUNDER, 2.0f, 1.2f);

        createTornadoEffect(center);

        for (Entity entity : center.getWorld().getNearbyEntities(center, TORNADO_RADIUS, TORNADO_HEIGHT, TORNADO_RADIUS)) {
            if (!(entity instanceof Player)) {
                continue;
            }

            Player target = (Player) entity;

            if (target.equals(player)) {
                continue;
            }

            User targetUser = (User) User.of(target.getUniqueId());

            if (targetUser == null || !targetUser.inState(ArcadeState.ALIVE)) {
                continue;
            }

            if (user.getTeam() != null && targetUser.getTeam() != null && user.getTeam().equals(targetUser.getTeam())) {
                continue;
            }

            Location targetLoc = target.getLocation();
            Vector direction = targetLoc.toVector().subtract(center.toVector()).normalize();

            direction.setY(0.8); // Reduzido de 1.2 para diminuir dano de queda

            double distance = targetLoc.distance(center);
            double force = 2.5 * (1.0 - (distance / TORNADO_RADIUS)); // Reduzido de 3.0 para 2.5
            force = Math.max(1.5, force); // Reduzido de 2.0 para 1.5

            Vector velocity = direction.multiply(force);
            target.setVelocity(velocity);

            // Adicionar proteção temporária contra dano de queda por 3 segundos
            target.setFallDistance(0);
            Core.getPlatform().runSync(() -> {
                if (target.isOnline()) {
                    target.setFallDistance(0);
                }
            }, 60L); // 3 segundos

            target.playSound(targetLoc, Sound.BAT_TAKEOFF, 1.5f, 0.6f);
        }
    }

    private void createTornadoEffect(Location center) {
        for (int tick = 0; tick < 60; tick++) {
            final int currentTick = tick;
            Core.getPlatform().runSync(() -> {
                double angle = currentTick * 0.6;
                double height = currentTick * 0.15;
                double radius = 2.5 + (currentTick * 0.04);

                for (int i = 0; i < 4; i++) {
                    double offsetAngle = angle + (i * Math.PI * 2 / 4);
                    double x = center.getX() + radius * Math.cos(offsetAngle);
                    double z = center.getZ() + radius * Math.sin(offsetAngle);
                    double y = center.getY() + height;

                    Location particleLoc = new Location(center.getWorld(), x, y, z);

                    ParticleBuilder particle = new ParticleBuilder(ParticleEffect.CLOUD, particleLoc);
                    particle.setSpeed(0.05f).setAmount(3).display();

                    if (currentTick % 5 == 0) {
                        ParticleBuilder smoke = new ParticleBuilder(ParticleEffect.SMOKE_LARGE, particleLoc);
                        smoke.setSpeed(0.1f).setAmount(2).display();
                    }

                    if (currentTick % 15 == 0) {
                        center.getWorld().playSound(center, Sound.FIZZ, 0.8f, 0.4f);
                    }
                }
            }, tick);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}
