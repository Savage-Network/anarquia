package com.minecraft.game.bukkit.cosmetic;

import com.minecraft.game.account.context.objects.cosmetic.Cape;
import com.minecraft.game.account.context.objects.cosmetic.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public final class ParticleRunner extends BukkitRunnable {

    private static final double VIEW_RADIUS_SQ = 48.0 * 48.0;
    private double angle = 0;

    @Override
    public void run() {
        angle += Math.PI / 12;
        if (angle >= 2 * Math.PI) angle = 0;

        renderCapes();
        renderAuras();
    }

    private void renderCapes() {
        Map<UUID, Cape> capes = CapeParticleRegistry.snapshot();
        if (capes.isEmpty()) return;

        for (Map.Entry<UUID, Cape> entry : capes.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;
            if (!CapeParticleRegistry.hasNearbyViewer(player, VIEW_RADIUS_SQ)) continue;
            CapeParticleRenderer.render(player, entry.getValue(), angle);
        }
    }

    private void renderAuras() {
        Map<UUID, ParticleEffect> active = CosmeticParticleRegistry.snapshot();
        if (active.isEmpty()) return;

        for (Map.Entry<UUID, ParticleEffect> entry : active.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;
            if (!CosmeticParticleRegistry.hasNearbyViewer(player, VIEW_RADIUS_SQ)) continue;

            spawnEffect(player, entry.getValue());
        }
    }

    private void spawnEffect(Player player, ParticleEffect effect) {
        Location base = player.getLocation();
        Particle particle = effect.getParticle();

        switch (effect) {
            case FLAME_AURA -> {
                for (int i = 0; i < 2; i++) {
                    double h = (System.currentTimeMillis() / 300.0 + i * 0.6) % 2.0;
                    double r = 0.45;
                    double x = r * Math.cos(angle + i * (2 * Math.PI / 3));
                    double z = r * Math.sin(angle + i * (2 * Math.PI / 3));
                    player.getWorld().spawnParticle(particle, base.getX() + x, base.getY() + h, base.getZ() + z, 1);
                }
            }
            case HEARTS -> {
                if (Math.random() < 0.12) {
                    double rx = (Math.random() - 0.5) * 1.1;
                    double ry = Math.random() * 2.0;
                    double rz = (Math.random() - 0.5) * 1.1;
                    player.getWorld().spawnParticle(particle, base.getX() + rx, base.getY() + ry, base.getZ() + rz, 1, 0, 0.05, 0);
                }
            }
            case PORTAL -> {
                double r = 0.55;
                for (int i = 0; i < 2; i++) {
                    double x = r * Math.cos(angle + i * Math.PI);
                    double z = r * Math.sin(angle + i * Math.PI);
                    player.getWorld().spawnParticle(particle, base.getX() + x, base.getY() + Math.random() * 1.8, base.getZ() + z, 1);
                }
            }
            case NOTES -> {
                double nx = 0.55 * Math.cos(angle);
                double nz = 0.55 * Math.sin(angle);
                player.getWorld().spawnParticle(particle, base.getX() + nx, base.getY() + 1.0, base.getZ() + nz,
                        0, Math.random(), 0.0, 0.0, 1.0);
            }
            case SMOKE_RING -> {
                double hx = 0.35 * Math.cos(angle * 2);
                double hz = 0.35 * Math.sin(angle * 2);
                player.getWorld().spawnParticle(particle, base.getX() + hx, base.getY() + 2.15, base.getZ() + hz, 1);
            }
            case HAPPY_VILLAGER -> {
                if (Math.random() < 0.2) {
                    double rx = (Math.random() - 0.5) * 1.2;
                    double ry = Math.random() * 1.8;
                    double rz = (Math.random() - 0.5) * 1.2;
                    player.getWorld().spawnParticle(particle, base.getX() + rx, base.getY() + ry, base.getZ() + rz, 1);
                }
            }
            case WITCH -> {
                double wx = 0.5 * Math.cos(-angle);
                double wz = 0.5 * Math.sin(-angle);
                player.getWorld().spawnParticle(particle, base.getX() + wx, base.getY() + Math.random() * 1.8, base.getZ() + wz, 1);
            }
            case SNOWFLAKE -> {
                if (Math.random() < 0.25) {
                    double rx = (Math.random() - 0.5) * 1.4;
                    double rz = (Math.random() - 0.5) * 1.4;
                    player.getWorld().spawnParticle(particle, base.getX() + rx, base.getY() + 2.2, base.getZ() + rz, 1, 0, -0.02, 0);
                }
            }
            case DRAGON_BREATH -> {
                if (Math.random() < 0.15) {
                    double rx = (Math.random() - 0.5) * 0.5;
                    double rz = (Math.random() - 0.5) * 0.5;
                    player.getWorld().spawnParticle(particle, base.getX() + rx, base.getY() + 0.15, base.getZ() + rz, 1, 0, 0.1, 0, 0.02);
                }
            }
            case ENCHANTED_HIT -> {
                double ex = 0.5 * Math.cos(angle * 2.5);
                double ez = 0.5 * Math.sin(angle * 2.5);
                double ey = 2.0 - ((System.currentTimeMillis() / 400.0) % 2.0);
                player.getWorld().spawnParticle(particle, base.getX() + ex, base.getY() + ey, base.getZ() + ez, 1);
            }
        }
    }
}
