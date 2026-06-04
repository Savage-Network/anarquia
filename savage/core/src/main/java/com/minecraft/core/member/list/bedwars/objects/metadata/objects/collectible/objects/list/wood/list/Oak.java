package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.wood.list;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.wood.WoodCollectible;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import org.bukkit.Material;

public class Oak extends WoodCollectible {

    public Oak() {
        super(CollectibleRarity.COMUM, "Carvalho",
                Item.of(Material.WOOD), 
                Material.WOOD, (byte) 0, 0L);
    }
}
