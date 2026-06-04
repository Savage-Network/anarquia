package com.minecraft.core.api.collectible.type.mutation;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import lombok.Getter;
import org.bukkit.Effect;
import org.bukkit.Material;

import java.util.Arrays;

@Getter
public enum MutationList {

    BRILHO("Brilho Místico", CollectibleRarity.RARE, Effect.HAPPY_VILLAGER, 6, true, Material.GLOWSTONE_DUST),
    FOGO("Chamas", CollectibleRarity.EPIC, Effect.MOBSPAWNER_FLAMES, 4, false, Material.BLAZE_POWDER),
    FUMACA("Fumaça Verde", CollectibleRarity.COMUM, Effect.SMOKE, 8, false, Material.SULPHUR),
    PORTAL("Energia do Portal", CollectibleRarity.MYTHICAL, Effect.PORTAL, 10, false, Material.ENDER_PEARL),
    RAIO("Energia Elétrica", CollectibleRarity.EPIC, Effect.ENDER_SIGNAL, 5, false, Material.BEACON),
    ARCO_IRIS("Arco-íris", CollectibleRarity.RARE, Effect.COLOURED_DUST, 8, false, Material.WOOL),
    FADA("Pó de Fada", CollectibleRarity.EPIC, Effect.FLYING_GLYPH, 7, true, Material.GLOWSTONE_DUST);

    private final MutationCollectible cosmetic;

    MutationList(String name, CollectibleRarity rarity, Effect effect, int amount, boolean glow, Material icon) {
        this.cosmetic = new MutationCollectible(name, rarity, Arrays.asList(RankType.MEMBER, RankType.VIP),
                effect, amount, glow, icon, System.currentTimeMillis());
        cosmetic.setLore(Arrays.asList(
                "§7Partículas constantes no corpo.",
                glow ? "§aInclui efeito de brilho." : "",
                "",
                "§7Raridade: " + rarity.getColoredName()));
    }
}
