package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.wood.list;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.wood.WoodCollectible;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import org.bukkit.Material;

public class Birch extends WoodCollectible {

    public Birch() {
        super(CollectibleRarity.COMUM, "Bétula",
                Item.of(Material.LOG, 2), 
                Material.LOG, (byte) 2, 0L);
    }
}
