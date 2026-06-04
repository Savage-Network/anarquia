package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class MedalTitle extends TitleCollectible {

    public MedalTitle() {
        super("Sua medalha", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.EMBER_PLUS), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Prévia:",
                "§3✖"
        ));
    }

    @Override
    public String getTitle() {
        return "§c§lSua medalha";
    }

    @Override
    public String getSubtitle() {
        return "§7Prévia:\n§3✖";
    }
}
