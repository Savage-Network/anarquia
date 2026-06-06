package com.minecraft.game.bukkit.cosmetic;

import com.minecraft.game.account.context.objects.cosmetic.Cape;
import com.minecraft.game.account.context.objects.cosmetic.CapeStyle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.awt.Color;

public final class CapeParticleRenderer {

    private CapeParticleRenderer() {}

    public static void render(Player player, Cape cape, double angle) {
        switch (cape.getStyle()) {
            case WINGS -> renderWings(player, cape, angle, false, 0.95);
            case WINGS_FIRE -> renderFireWings(player, cape, angle);
            case WINGS_RAINBOW -> renderRainbowWings(player, angle);
            case HALO -> renderHalo(player, cape, angle);
            case HEART -> renderHeart(player, cape, angle);
            case DEMON -> renderWings(player, cape, angle, true, 1.05);
            case SPARKLE -> renderSparkleWings(player, cape, angle);
        }
    }

    private static void renderWings(Player player, Cape cape, double angle, boolean demon, double spread) {
        WingBasis basis = WingBasis.of(player);
        Color color = cape.getColor();
        Particle.DustOptions dust = dust(color, demon ? 1.2f : 1.0f);
        int feathers = demon ? 11 : 9;
        double flutter = Math.sin(angle * 3.0) * 0.14;

        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < feathers; i++) {
                double t = i / (double) (feathers - 1);
                double up = 0.25 + t * 1.15;
                double out = side * t * spread;
                double curve = Math.sin(t * Math.PI) * 0.18;
                double fx = flutter * (1.0 - t);

                double x = basis.x + basis.backX * (0.38 - curve) + basis.rightX * (out + fx);
                double y = basis.y + up;
                double z = basis.z + basis.backZ * (0.38 - curve) + basis.rightZ * (out + fx);
                spawnDust(player, x, y, z, dust);
            }
        }
    }

    private static void renderFireWings(Player player, Cape cape, double angle) {
        WingBasis basis = WingBasis.of(player);
        double flutter = Math.sin(angle * 4.0) * 0.1;
        int feathers = 10;

        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < feathers; i++) {
                double t = i / (double) (feathers - 1);
                double up = 0.2 + t * 1.2;
                double out = side * t * 0.9;
                double curve = Math.sin(t * Math.PI) * 0.2;

                double x = basis.x + basis.backX * (0.35 - curve) + basis.rightX * (out + flutter);
                double y = basis.y + up;
                double z = basis.z + basis.backZ * (0.35 - curve) + basis.rightZ * (out + flutter);

                player.getWorld().spawnParticle(Particle.FLAME, x, y, z, 1, 0.02, 0.04, 0.02, 0.01);
                if (i % 2 == 0) {
                    spawnDust(player, x, y, z, dust(cape.getColor(), 1.1f));
                }
                if (t > 0.55 && Math.random() < 0.35) {
                    player.getWorld().spawnParticle(Particle.SMOKE, x, y + 0.05, z, 1, 0, 0.02, 0, 0.01);
                }
            }
        }
    }

    private static void renderRainbowWings(Player player, double angle) {
        WingBasis basis = WingBasis.of(player);
        double flutter = Math.sin(angle * 3.0) * 0.12;
        int feathers = 9;

        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < feathers; i++) {
                double t = i / (double) (feathers - 1);
                double up = 0.25 + t * 1.15;
                double out = side * t * 1.0;
                double curve = Math.sin(t * Math.PI) * 0.16;
                float hue = (float) ((angle / (2 * Math.PI) + t * 0.35 + (side > 0 ? 0.5f : 0f)) % 1.0);
                Color rgb = Color.getHSBColor(hue, 0.85f, 1.0f);

                double x = basis.x + basis.backX * (0.36 - curve) + basis.rightX * (out + flutter);
                double y = basis.y + up;
                double z = basis.z + basis.backZ * (0.36 - curve) + basis.rightZ * (out + flutter);
                spawnDust(player, x, y, z, dust(rgb, 1.0f));
            }
        }
    }

    private static void renderHalo(Player player, Cape cape, double angle) {
        WingBasis basis = WingBasis.of(player);
        int points = 14;
        double radius = 0.55;
        double yOff = 1.05;

        for (int i = 0; i < points; i++) {
            double t = i / (double) points * 2 * Math.PI + angle;
            double lx = Math.sin(t) * radius;
            double ly = Math.cos(t) * radius * 0.35;
            double x = basis.x + basis.backX * 0.45 + basis.rightX * lx;
            double y = basis.y + yOff + ly;
            double z = basis.z + basis.backZ * 0.45 + basis.rightZ * lx;

            player.getWorld().spawnParticle(Particle.FLAME, x, y, z, 1, 0, 0.02, 0, 0.01);
            if (i % 2 == 0) {
                spawnDust(player, x, y, z, dust(cape.getColor(), 1.0f));
            }
        }
    }

    private static void renderHeart(Player player, Cape cape, double angle) {
        WingBasis basis = WingBasis.of(player);
        int points = 16;
        double scale = 0.11;
        double pulse = 1.0 + Math.sin(angle * 2.0) * 0.08;

        for (int i = 0; i < points; i++) {
            double t = i / (double) points * 2 * Math.PI;
            double hx = 16 * Math.pow(Math.sin(t), 3);
            double hy = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);

            double lx = hx * scale * pulse;
            double ly = hy * scale * pulse * 0.55 + 1.0;

            double x = basis.x + basis.backX * 0.5 + basis.rightX * lx;
            double y = basis.y + ly;
            double z = basis.z + basis.backZ * 0.5 + basis.rightZ * lx;
            spawnDust(player, x, y, z, dust(cape.getColor(), 1.1f));
        }
    }

    private static void renderSparkleWings(Player player, Cape cape, double angle) {
        renderWings(player, cape, angle, false, 1.0);
        WingBasis basis = WingBasis.of(player);
        if (Math.random() > 0.45) return;

        for (int side = -1; side <= 1; side += 2) {
            double t = Math.random();
            double up = 0.4 + t * 0.9;
            double out = side * t * 0.85;
            double x = basis.x + basis.backX * 0.4 + basis.rightX * out;
            double y = basis.y + up;
            double z = basis.z + basis.backZ * 0.4 + basis.rightZ * out;
            player.getWorld().spawnParticle(Particle.END_ROD, x, y, z, 1, 0, 0, 0, 0.01);
            spawnDust(player, x, y, z, dust(cape.getColor(), 0.8f));
        }
    }

    private static void spawnDust(Player player, double x, double y, double z, Particle.DustOptions dust) {
        player.getWorld().spawnParticle(Particle.DUST, x, y, z, 1, 0, 0, 0, 0, dust);
    }

    private static Particle.DustOptions dust(Color color, float size) {
        return new Particle.DustOptions(
                org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue()), size);
    }

    private record WingBasis(double x, double y, double z, double backX, double backZ, double rightX, double rightZ) {
        static WingBasis of(Player player) {
            Location base = player.getLocation();
            double yaw = Math.toRadians(base.getYaw());
            return new WingBasis(
                    base.getX(), base.getY(), base.getZ(),
                    -Math.sin(yaw), Math.cos(yaw),
                    Math.cos(yaw), Math.sin(yaw));
        }
    }
}
