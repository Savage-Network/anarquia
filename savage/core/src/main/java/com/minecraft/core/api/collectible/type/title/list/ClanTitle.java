package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.CollectibleTitleGroup;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class ClanTitle extends TitleCollectible {

    public ClanTitle() {
        super("Clan", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.MEMBER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "",
                "§eMostra acima do nome:",
                "",
                "§f§o§cClan§8: §r§eMembro de §7[ sua tag ]",
                "",
                "§7Disponível para quem pode entrar em clã.",
                "",
                "§8Exemplo: §r§f§o§cClan§8: §r§eMembro de §d[L9K]"
        ));
    }

    @Override
    public CollectibleTitleGroup getTitleGroup() {
        return CollectibleTitleGroup.SPECIAL;
    }

    @Override
    public String getTitle() {
        return "§c§lClan";
    }

    @Override
    public String getSubtitle() {
        return "§f§o§eMembro de §7[SuaTAG]";
    }
}
