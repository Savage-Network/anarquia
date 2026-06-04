package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.CollectibleTitleGroup;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class BedWarsLevelTitle extends TitleCollectible {

    public BedWarsLevelTitle() {
        super("Nível Bed Wars", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.EMBER_PLUS), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§cBed Wars",
                "",
                "§7Mostre seu nível no Bed Wars",
                "§7para todos verem!",
                "",
                "§7Incluso no §c§lEmber+"
        ));
    }

    @Override
    public CollectibleTitleGroup getTitleGroup() {
        return CollectibleTitleGroup.BED_WARS;
    }

    @Override
    public String getTitle() {
        return "§cNível Bed Wars";
    }

    @Override
    public String getSubtitle() {
        return null;
    }
}
