package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list;

import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;

public class LightningStrike extends BedDestroyCollectible {

    public LightningStrike() {
        super(CollectibleRarity.RARE, "Raio",
                Item.of(Material.BLAZE_ROD), 0L);
    }

    @Override
    public void execute(Location location) {
        location.getWorld().strikeLightningEffect(location);
        location.getWorld().playEffect(location, Effect.EXPLOSION_HUGE, 1);
    }
}
