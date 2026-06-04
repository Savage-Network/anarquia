package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.wood.list;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.wood.WoodCollectible;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import org.bukkit.Material;

public class DarkOak extends WoodCollectible {

    public DarkOak() {
        super(CollectibleRarity.EPIC, "Carvalho Escuro", 
                Item.of(Material.LOG_2, 1), 
                Material.LOG_2, (byte) 1, 0L);
    }
}
