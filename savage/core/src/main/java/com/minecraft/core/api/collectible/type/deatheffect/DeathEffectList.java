package com.minecraft.core.api.collectible.type.deatheffect;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import lombok.Getter;
import org.bukkit.Effect;
import org.bukkit.Material;

import java.util.Arrays;

@Getter
public enum DeathEffectList {

    FUMACA("Morte Sombria", CollectibleRarity.COMUM, Effect.SMOKE, 10, Material.SULPHUR),
    EXPLOSAO("Morte Explosiva", CollectibleRarity.RARE, Effect.EXPLOSION_LARGE, 3, Material.TNT),
    CORACOES("Morte Romântica", CollectibleRarity.RARE, Effect.HEART, 8, Material.RED_ROSE),
    RAIO("Morte Elétrica", CollectibleRarity.EPIC, Effect.ENDER_SIGNAL, 6, Material.BEACON),
    FADA("Morte da Fada", CollectibleRarity.MYTHICAL, Effect.FLYING_GLYPH, 12, Material.NETHER_STAR);

    private final DeathEffectCollectible cosmetic;

    DeathEffectList(String name, CollectibleRarity rarity, Effect effect, int amount, Material icon) {
        this.cosmetic = new DeathEffectCollectible(name, rarity, Arrays.asList(RankType.MEMBER, RankType.VIP),
                effect, amount, icon, System.currentTimeMillis());
        cosmetic.setLore(Arrays.asList("§7Efeito ao morrer no lobby.", "", "§7Raridade: " + rarity.getColoredName()));
    }
}
