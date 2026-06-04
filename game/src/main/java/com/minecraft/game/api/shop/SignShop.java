package com.minecraft.game.api.shop;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

@Getter
public final class SignShop {

    private final String id;
    private final String world;
    private final int x;
    private final int y;
    private final int z;
    private final UUID ownerId;
    private final SignShopType type;
    private final Material material;
    private final int pricePerUnit;
    private final boolean serverShop;

    public SignShop(String id, String world, int x, int y, int z, UUID ownerId,
                    SignShopType type, Material material, int pricePerUnit, boolean serverShop) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.ownerId = ownerId;
        this.type = type;
        this.material = material;
        this.pricePerUnit = pricePerUnit;
        this.serverShop = serverShop;
    }

    public Location location(org.bukkit.World w) {
        return new Location(w, x, y, z);
    }

    public static String locationId(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }
}
