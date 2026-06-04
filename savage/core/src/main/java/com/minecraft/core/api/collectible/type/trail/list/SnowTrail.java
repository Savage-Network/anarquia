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

public class SnowTrail extends TrailCollectible {

    public SnowTrail() {
        super("Neve", CollectibleRarity.COMUM, Arrays.asList(RankType.MEMBER), System.currentTimeMillis());
        setIcon(Item.of(Material.SNOW_BALL));
        setLore(Arrays.asList("§7Rastro de neve ao caminhar.", "", "§7Raridade: §fComum"));
    }

    @Override
    public void applyTrail(Player player, Location location) {
        player.getWorld().playEffect(location.clone().subtract(0, 0.1, 0), Effect.SNOWBALL_BREAK, 0);
    }

    @Override
    public void removeTrail(Location location) {
    }
}
