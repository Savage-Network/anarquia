package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.CollectibleTitleGroup;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class StaffHighlightTitle extends TitleCollectible {

    public StaffHighlightTitle() {
        super("Destaque", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.ADMIN), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§6Staff do Mês",
                "",
                "§eVocê foi reconhecido como",
                "§eo melhor staff do mês!",
                "",
                "§7Exclusivo para §6§lStaff Destaque"
        ));
    }

    @Override
    public CollectibleTitleGroup getTitleGroup() {
        return CollectibleTitleGroup.SPECIAL;
    }

    @Override
    public String getTitle() {
        return "§6§l✦ §e§lSTAFF DESTAQUE §6§l✦";
    }

    @Override
    public String getSubtitle() {
        return null;
    }
}
