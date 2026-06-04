package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.wood.list;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.wood.WoodCollectible;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import org.bukkit.Material;

public class Acacia extends WoodCollectible {

    public Acacia() {
        super(CollectibleRarity.RARE, "Acácia", 
                Item.of(Material.LOG_2, 0), 
                Material.LOG_2, (byte) 0, 0L);
    }
}
