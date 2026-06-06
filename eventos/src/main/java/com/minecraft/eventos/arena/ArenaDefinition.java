package com.minecraft.eventos.arena;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/** Definicao persistida de uma arena. */
@Getter
@Setter
public final class ArenaDefinition {

    private String id;
    private String modeId;
    private String worldName;
    private double centerX;
    private double centerY;
    private double centerZ;
    private float centerYaw;
    private float centerPitch;
    private int radius = 14;
    private int dieY = 50;
    private int width = 15;
    private int depth = 15;
    private boolean generated;
    private long generatedAt;
    private List<SpawnPoint> spawns = new ArrayList<>();

    public Location center() {
        World w = Bukkit.getWorld(worldName);
        if (w == null) return null;
        return new Location(w, centerX, centerY, centerZ, centerYaw, centerPitch);
    }

    public void setCenter(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        worldName = loc.getWorld().getName();
        centerX = loc.getX();
        centerY = loc.getY();
        centerZ = loc.getZ();
        centerYaw = loc.getYaw();
        centerPitch = loc.getPitch();
    }

    public Location primarySpawn() {
        if (!spawns.isEmpty()) {
            SpawnPoint sp = spawns.get(0);
            World w = Bukkit.getWorld(sp.worldName() != null ? sp.worldName() : worldName);
            if (w != null) {
                return new Location(w, sp.x(), sp.y(), sp.z(), sp.yaw(), sp.pitch());
            }
        }
        Location c = center();
        if (c != null) return c.clone().add(0.5, 1, 0.5);
        return null;
    }

    public record SpawnPoint(String worldName, double x, double y, double z, float yaw, float pitch) {
        public static SpawnPoint of(Location loc) {
            return new SpawnPoint(
                    loc.getWorld().getName(),
                    loc.getX(), loc.getY(), loc.getZ(),
                    loc.getYaw(), loc.getPitch());
        }
    }
}
