package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.CollectibleTitleGroup;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class Top1BedWarsTitle extends TitleCollectible {

    public Top1BedWarsTitle() {
        super("Campeão", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.ADMIN), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§6Temporada 1",
                "",
                "§eVocê foi o melhor jogador",
                "§eda temporada!",
                "",
                "§7Exclusivo para §6§lTop 1"
        ));
    }

    @Override
    public CollectibleTitleGroup getTitleGroup() {
        return CollectibleTitleGroup.BED_WARS;
    }

    @Override
    public String getTitle() {
        return "§6§lCAMPEÃO";
    }

    @Override
    public String getSubtitle() {
        return null;
    }
}
