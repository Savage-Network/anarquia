package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.win.list;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.win.WinAnimationCollectible;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import org.bukkit.Material;

public class Fireworks extends WinAnimationCollectible {

    public Fireworks() {
        super(CollectibleRarity.COMUM, "Fogos de Artifício",
                Item.of(Material.FIREWORK), 
                "FIREWORKS", 0L);
    }
}
