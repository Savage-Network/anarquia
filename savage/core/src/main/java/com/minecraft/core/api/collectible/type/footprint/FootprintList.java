package com.minecraft.core.api.collectible.type.footprint;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import lombok.Getter;
import org.bukkit.Effect;
import org.bukkit.Material;

import java.util.Arrays;

@Getter
public enum FootprintList {

    FELIZ("Pegadas Felizes", CollectibleRarity.COMUM, Effect.HAPPY_VILLAGER, 4, Material.SAPLING),
    FOGO("Pegadas de Fogo", CollectibleRarity.RARE, Effect.MOBSPAWNER_FLAMES, 3, Material.BLAZE_POWDER),
    NOTAS("Pegadas Musicais", CollectibleRarity.RARE, Effect.NOTE, 5, Material.NOTE_BLOCK),
    PORTAL("Pegadas do Portal", CollectibleRarity.EPIC, Effect.PORTAL, 6, Material.ENDER_PEARL),
    NEVOA("Pegadas de Nuvem", CollectibleRarity.EPIC, Effect.CLOUD, 4, Material.WOOL),
    SAVAGE("Pegadas Savage", CollectibleRarity.MYTHICAL, Effect.VILLAGER_THUNDERCLOUD, 5, Material.SLIME_BALL);

    private final FootprintCollectible cosmetic;

    FootprintList(String name, CollectibleRarity rarity, Effect effect, int amount, Material icon) {
        this.cosmetic = new FootprintCollectible(name, rarity, Arrays.asList(RankType.MEMBER, RankType.VIP),
                effect, amount, icon, System.currentTimeMillis());
        cosmetic.setLore(Arrays.asList("§7Rastro nos pés ao andar.", "", "§7Raridade: " + rarity.getColoredName()));
    }
}
