package com.minecraft.core.api.collectible.type.aura;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import lombok.Getter;
import org.bukkit.Effect;
import org.bukkit.Material;

import java.util.Arrays;

@Getter
public enum AuraList {

    CORACOES("Aura Corações", CollectibleRarity.RARE, Effect.HEART, 1.2, 8, Material.RED_ROSE),
    NOTAS("Aura Musical", CollectibleRarity.EPIC, Effect.NOTE, 1.4, 10, Material.NOTE_BLOCK),
    FADA("Aura Fada", CollectibleRarity.MYTHICAL, Effect.FLYING_GLYPH, 1.5, 12, Material.NETHER_STAR),
    ESTRELAS("Aura Estrelas", CollectibleRarity.EPIC, Effect.CRIT, 1.3, 10, Material.NETHER_STAR),
    NEVE("Aura Nevasca", CollectibleRarity.RARE, Effect.SNOWBALL_BREAK, 1.2, 8, Material.SNOW_BALL);

    private final AuraCollectible cosmetic;

    AuraList(String name, CollectibleRarity rarity, Effect effect, double radius, int points, Material icon) {
        this.cosmetic = new AuraCollectible(name, rarity, Arrays.asList(RankType.MEMBER, RankType.VIP),
                effect, radius, points, icon, System.currentTimeMillis());
        cosmetic.setLore(Arrays.asList(
                "§7Anel de partículas ao redor do jogador.",
                "",
                "§7Raridade: " + rarity.getColoredName()));
    }
}
