package com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.win.list;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.win.WinAnimationCollectible;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import org.bukkit.Material;

public class EnderDragon extends WinAnimationCollectible {

    public EnderDragon() {
        super(CollectibleRarity.MYTHICAL, "Ender Dragon",
                Item.of(Material.DRAGON_EGG), 
                "ENDER_DRAGON", 0L);
    }
}
