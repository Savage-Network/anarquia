package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.wood.list;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.wood.WoodCollectible;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import org.bukkit.Material;

public class Jungle extends WoodCollectible {

    public Jungle() {
        super(CollectibleRarity.RARE, "Selva", 
                Item.of(Material.LOG, 3), 
                Material.LOG, (byte) 3, 0L);
    }
}
