package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.wood;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

@Getter
@Setter
public class WoodCollectible extends BedCollectible {

    private Material woodType;
    private byte woodData;

    public WoodCollectible(CollectibleRarity rarity, String name, Item icon, Material woodType, byte woodData, long createdAt) {
        super(BedCollectibleType.WOOD_TYPE, rarity, name, icon, createdAt);
        this.woodType = woodType;
        this.woodData = woodData;
    }
}
