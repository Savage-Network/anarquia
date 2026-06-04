package com.minecraft.core.api.collectible.type.trail.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.trail.TrailCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class WaterTrail extends TrailCollectible {

    public WaterTrail() {
        super("Água", CollectibleRarity.RARE, Arrays.asList(RankType.MEMBER, RankType.VIP), System.currentTimeMillis());
        setIcon(Item.of(Material.WATER_BUCKET));
        setLore(Arrays.asList("§7Gotas de água no chão.", "", "§7Raridade: §9Raro"));
    }

    @Override
    public void applyTrail(Player player, Location location) {
        for (int i = 0; i < 3; i++) {
            double ox = (Math.random() - 0.5) * 0.5;
            double oz = (Math.random() - 0.5) * 0.5;
            player.getWorld().playEffect(location.clone().add(ox, -0.1, oz), Effect.WATERDRIP, 0);
        }
    }

    @Override
    public void removeTrail(Location location) {
    }
}
