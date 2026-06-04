package com.minecraft.lobbyhost.architect;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Localizacao nomeada do {@code architect.json}. Espelha
 * {@code SignedLocation} do Savage.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignedLocation {

    private String name;
    private String world = "world";
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public Location toBukkit() {
        return new Location(Bukkit.getWorld(world == null ? "world" : world),
                x, y, z, yaw, pitch);
    }
}
