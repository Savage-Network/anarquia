package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class PingTitle extends TitleCollectible {

    public PingTitle() {
        super("Ping", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.EMBER_PLUS), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Prévia:",
                "§ePing: §a9ms"
        ));
    }

    @Override
    public String getTitle() {
        return "§c§lPing";
    }

    @Override
    public String getSubtitle() {
        return "§7Prévia:\n§ePing: §a9ms";
    }
}
