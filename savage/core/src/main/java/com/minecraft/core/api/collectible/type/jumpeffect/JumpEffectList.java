package com.minecraft.core.api.collectible.type.jumpeffect;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import lombok.Getter;
import org.bukkit.Effect;
import org.bukkit.Material;

import java.util.Arrays;

@Getter
public enum JumpEffectList {

    NUVEM("Pulo das Nuvens", CollectibleRarity.COMUM, Effect.CLOUD, 6, Material.FEATHER),
    EXPLOSAO("Pulo Explosivo", CollectibleRarity.RARE, Effect.EXPLOSION_LARGE, 2, Material.TNT),
    CORACOES("Pulo Apaixonado", CollectibleRarity.RARE, Effect.HEART, 5, Material.RED_ROSE),
    PORTAL("Pulo Dimensional", CollectibleRarity.EPIC, Effect.PORTAL, 8, Material.ENDER_PEARL),
    CRITICO("Pulo Crítico", CollectibleRarity.EPIC, Effect.CRIT, 10, Material.DIAMOND_SWORD),
    SAVAGE("Pulo Savage", CollectibleRarity.MYTHICAL, Effect.FIREWORKS_SPARK, 12, Material.FIREWORK);

    private final JumpEffectCollectible cosmetic;

    JumpEffectList(String name, CollectibleRarity rarity, Effect effect, int amount, Material icon) {
        this.cosmetic = new JumpEffectCollectible(name, rarity, Arrays.asList(RankType.MEMBER, RankType.VIP),
                effect, amount, icon, System.currentTimeMillis());
        cosmetic.setLore(Arrays.asList("§7Partículas ao pular.", "", "§7Raridade: " + rarity.getColoredName()));
    }
}
