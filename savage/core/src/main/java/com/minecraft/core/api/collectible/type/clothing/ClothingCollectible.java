package com.minecraft.core.api.collectible.type.clothing;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

@Getter
public class ClothingCollectible extends Collectible {

    private final Color color;
    private final boolean helmet;
    private final boolean chest;
    private final boolean legs;
    private final boolean boots;

    public ClothingCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, Color color,
                               boolean helmet, boolean chest, boolean legs, boolean boots, long releasedAt) {
        super(name, CollectibleCategory.CLOTHING, rarity, ranks, releasedAt);
        this.color = color;
        this.helmet = helmet;
        this.chest = chest;
        this.legs = legs;
        this.boots = boots;
        setIcon(Item.of(Material.LEATHER_CHESTPLATE).color(color));
        Core.getCollectibleController().save(this);
    }

    public ItemStack piece(Material material) {
        return Item.of(material).color(color).make();
    }

    public static void handle() {
        for (ClothingList outfit : ClothingList.values())
            Core.getCollectibleController().save(outfit.getCosmetic());
    }
}
