package com.minecraft.game.account.context.objects.home;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

@Getter
@Setter
public class StoredLocation {

    private String world = "world";
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public static StoredLocation from(Location loc) {
        StoredLocation s = new StoredLocation();
        s.world = loc.getWorld() != null ? loc.getWorld().getName() : "world";
        s.x = loc.getX();
        s.y = loc.getY();
        s.z = loc.getZ();
        s.yaw = loc.getYaw();
        s.pitch = loc.getPitch();
        return s;
    }

    public Location toBukkit() {
        World w = Bukkit.getWorld(world);
        if (w == null) w = Bukkit.getWorlds().getFirst();
        return new Location(w, x, y, z, yaw, pitch);
    }
}
