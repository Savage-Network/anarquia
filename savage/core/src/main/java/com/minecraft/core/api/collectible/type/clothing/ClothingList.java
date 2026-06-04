package com.minecraft.core.api.collectible.type.clothing;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;

import java.util.Arrays;

@Getter
public enum ClothingList {

    RUBI("Traje Rubi", CollectibleRarity.RARE, Color.fromRGB(200, 0, 50)),
    OCEANO("Traje Oceano", CollectibleRarity.RARE, Color.fromRGB(0, 120, 200)),
    FLORESTA("Traje Floresta", CollectibleRarity.COMUM, Color.fromRGB(0, 160, 80)),
    OURO("Traje Ouro", CollectibleRarity.EPIC, Color.fromRGB(255, 200, 0)),
    SAVAGE("Traje Savage", CollectibleRarity.MYTHICAL, Color.fromRGB(0, 100, 0)),
    ROXO("Traje Roxo", CollectibleRarity.EPIC, Color.fromRGB(140, 0, 200)),
    PRETO("Traje Noturno", CollectibleRarity.RARE, Color.fromRGB(30, 30, 30)),
    ROSA("Traje Rosa", CollectibleRarity.RARE, Color.fromRGB(255, 105, 180));

    private final ClothingCollectible cosmetic;

    ClothingList(String name, CollectibleRarity rarity, Color color) {
        this.cosmetic = new ClothingCollectible(name, rarity, Arrays.asList(RankType.MEMBER, RankType.VIP),
                color, true, true, true, true, System.currentTimeMillis());
        cosmetic.setLore(Arrays.asList(
                "§7Conjunto completo de couro tingido",
                "§7apenas visual no lobby.",
                "",
                "§7Raridade: " + rarity.getColoredName()));
    }
}
