package com.minecraft.game.anticheat;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/** Cálculos de combate/movimento (inspirado em GrimAC / Vulcan). */
public final class AnticheatMath {

    private AnticheatMath() {}

    /** Distância do olho do atacante até a hitbox do alvo (reach real). */
    public static double hitboxReach(Player attacker, Entity victim) {
        if (attacker == null || victim == null) return 0;
        Location eye = attacker.getEyeLocation();
        BoundingBox box = victim.getBoundingBox();
        return distancePointToBox(eye.getX(), eye.getY(), eye.getZ(), box);
    }

    /** Distância horizontal olho → hitbox (ignora Y). */
    public static double horizontalHitboxReach(Player attacker, Entity victim) {
        if (attacker == null || victim == null) return 0;
        Location eye = attacker.getEyeLocation();
        BoundingBox box = victim.getBoundingBox();
        double dx = clampDistance(eye.getX(), box.getMinX(), box.getMaxX());
        double dz = clampDistance(eye.getZ(), box.getMinZ(), box.getMaxZ());
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double distancePointToBox(double x, double y, double z, BoundingBox box) {
        double dx = clampDistance(x, box.getMinX(), box.getMaxX());
        double dy = clampDistance(y, box.getMinY(), box.getMaxY());
        double dz = clampDistance(z, box.getMinZ(), box.getMaxZ());
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static double clampDistance(double point, double min, double max) {
        if (point < min) return min - point;
        if (point > max) return point - max;
        return 0;
    }

    /** Linha de visão bloqueada por bloco sólido entre atacante e vítima. */
    public static boolean hasBlockObstruction(Player attacker, Entity victim) {
        if (attacker == null || victim == null) return false;
        World world = attacker.getWorld();
        Location eye = attacker.getEyeLocation();
        Location target = victim.getLocation().add(0, victim.getHeight() * 0.5, 0);
        Vector dir = target.toVector().subtract(eye.toVector());
        double len = dir.length();
        if (len < 0.01) return false;
        dir.normalize();
        RayTraceResult trace = world.rayTraceBlocks(
                eye, dir, len - 0.1, FluidCollisionMode.NEVER, true);
        return trace != null && trace.getHitBlock() != null;
    }

    /** Delta de rotação entre duas posições (graus). */
    public static float rotationDelta(float from, float to) {
        float diff = Math.abs(wrapAngle(to) - wrapAngle(from));
        return diff > 180f ? 360f - diff : diff;
    }

    public static float totalRotationDelta(float yaw1, float pitch1, float yaw2, float pitch2) {
        return rotationDelta(yaw1, yaw2) + rotationDelta(pitch1, pitch2) * 0.5f;
    }

    private static float wrapAngle(float angle) {
        angle %= 360f;
        if (angle >= 180f) angle -= 360f;
        if (angle < -180f) angle += 360f;
        return angle;
    }

    /** Jogador em bloco que reduz velocidade (cobweb, soul sand, honey). */
    public static boolean inSlowBlock(Player player) {
        if (player == null) return false;
        Block feet = player.getLocation().getBlock();
        Block head = player.getLocation().clone().add(0, 1, 0).getBlock();
        return isSlowMaterial(feet) || isSlowMaterial(head);
    }

    private static boolean isSlowMaterial(Block block) {
        if (block == null) return false;
        String name = block.getType().name();
        return name.contains("COBWEB") || name.equals("SOUL_SAND") || name.equals("SOUL_SOIL")
                || name.contains("HONEY") || name.equals("SLIME_BLOCK");
    }

    /** Bloco sólido sob os pés (com tolerância). */
    public static boolean hasSolidGround(Player player, Location loc, double tolerance) {
        if (player == null || loc == null) return false;
        if (player.isOnGround() || player.isInWater() || player.isClimbing() || player.isSwimming()) {
            return true;
        }
        Block below = loc.clone().subtract(0, tolerance, 0).getBlock();
        return below.getType().isSolid();
    }
}
