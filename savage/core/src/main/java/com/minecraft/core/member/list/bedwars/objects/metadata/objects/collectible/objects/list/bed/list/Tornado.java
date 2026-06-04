package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;

public class Tornado extends BedDestroyCollectible {

    public Tornado() {
        super(CollectibleRarity.EPIC, "Tornado",
                Item.of(Material.FEATHER), 0L);
    }

    @Override
    public void execute(Location location) {
        final int maxHeight = 5;
        final double maxRadius = 3.0;
        final int lines = 4;
        final double heightIncrement = 0.25;
        final double radiusIncrement = maxRadius / maxHeight;

        for (int tick = 0; tick < 40; tick++) {
            final int currentTick = tick;
            Core.getPlatform().runSync(() -> {
                for (int l = 0; l < lines; l++) {
                    for (double y = 0; y < maxHeight; y += heightIncrement) {
                        double radius = y * radiusIncrement;
                        double x = Math.cos(Math.toRadians((360.0 / lines * l) + y * 30.0 - currentTick)) * radius;
                        double z = Math.sin(Math.toRadians((360.0 / lines * l) + y * 30.0 - currentTick)) * radius;
                        location.getWorld().playEffect(location.clone().add(x, y, z), Effect.CLOUD, 1);
                    }
                }
            }, tick);
        }
    }
}
