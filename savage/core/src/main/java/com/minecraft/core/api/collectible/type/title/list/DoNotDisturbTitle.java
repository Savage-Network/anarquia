package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class DoNotDisturbTitle extends TitleCollectible {

    public DoNotDisturbTitle() {
        super("Não Perturbe", CollectibleRarity.COMUM, Arrays.asList(RankType.MEMBER), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Gerais",
                "",
                "§7Mostre que você não quer",
                "§7ser incomodado!",
                "",
                "§7Disponível para §a§lTodos"
        ));
    }

    @Override
    public String getTitle() {
        return "§c✖";
    }

    @Override
    public String getSubtitle() {
        return "§7Gerais";
    }
}
