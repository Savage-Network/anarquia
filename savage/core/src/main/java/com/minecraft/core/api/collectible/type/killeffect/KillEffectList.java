package com.minecraft.core.api.collectible.type.killeffect;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import lombok.Getter;
import org.bukkit.Effect;
import org.bukkit.Material;

import java.util.Arrays;

@Getter
public enum KillEffectList {

    EXPLOSAO("Explosão", CollectibleRarity.RARE, Effect.EXPLOSION_LARGE, 3, Material.TNT),
    RAIO("Raio", CollectibleRarity.EPIC, Effect.ENDER_SIGNAL, 5, Material.BEACON),
    SANGUE("Sangue", CollectibleRarity.COMUM, Effect.STEP_SOUND, 8, Material.REDSTONE),
    FOGO("Chamas", CollectibleRarity.RARE, Effect.MOBSPAWNER_FLAMES, 6, Material.BLAZE_POWDER),
    FADA("Fada", CollectibleRarity.EPIC, Effect.FLYING_GLYPH, 10, Material.NETHER_STAR),
    SAVAGE("Abate Savage", CollectibleRarity.MYTHICAL, Effect.CRIT, 12, Material.DIAMOND_SWORD);

    private final KillEffectCollectible cosmetic;

    KillEffectList(String name, CollectibleRarity rarity, Effect effect, int amount, Material icon) {
        this.cosmetic = new KillEffectCollectible(name, rarity, Arrays.asList(RankType.MEMBER, RankType.VIP),
                effect, amount, icon, System.currentTimeMillis());
        cosmetic.setLore(Arrays.asList(
                "§7Efeito ao abater no lobby.",
                "",
                "§7Raridade: " + rarity.getColoredName()));
    }
}
