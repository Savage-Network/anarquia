package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class SinceTitle extends TitleCollectible {

    public SinceTitle() {
        super("Desde", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.EMBER_PLUS), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§6Gerais",
                "",
                "§7Mostre desde quando você",
                "§7joga no servidor!",
                "",
                "§7Incluso no §c§lEmber+"
        ));
    }

    @Override
    public String getTitle() {
        return "§cDesde";
    }

    @Override
    public String getSubtitle() {
        return null;
    }
}
