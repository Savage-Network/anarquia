package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;

public class Explosions extends BedDestroyCollectible {

    public Explosions() {
        super(CollectibleRarity.COMUM, "Explosões",
                Item.of(Material.TNT), 0L);
    }

    @Override
    public void execute(Location location) {
        for (int i = 0; i < 5; i++) {
            final int delay = i * 4; // 0.2 segundos entre cada explosão
            Core.getPlatform().runSync(() -> {
                location.getWorld().playEffect(location.clone().add(0, 1, 0), Effect.EXPLOSION_LARGE, 1);
                location.getWorld().playSound(location, Sound.EXPLODE, 0.5f, 1.0f);
            }, delay);
        }
    }
}
