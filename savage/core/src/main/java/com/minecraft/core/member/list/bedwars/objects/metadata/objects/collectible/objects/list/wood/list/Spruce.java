package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.wood.list;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.wood.WoodCollectible;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import org.bukkit.Material;

public class Spruce extends WoodCollectible {

    public Spruce() {
        super(CollectibleRarity.COMUM, "Abeto",
                Item.of(Material.LOG, 1), 
                Material.LOG, (byte) 1, 0L);
    }
}
