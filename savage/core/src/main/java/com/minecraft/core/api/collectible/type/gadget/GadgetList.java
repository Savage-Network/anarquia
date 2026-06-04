package com.minecraft.core.api.collectible.type.gadget;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import lombok.Getter;
import org.bukkit.Material;

import java.util.Arrays;

@Getter
public enum GadgetList {

    VARINHA("Varinha Mágica", CollectibleRarity.RARE, Material.BLAZE_ROD, (short) 0, 8),
    ROSA("Rosa Dourada", CollectibleRarity.COMUM, Material.RED_ROSE, (short) 0, 8),
    RELÓGIO("Relógio de Bolso", CollectibleRarity.RARE, Material.WATCH, (short) 0, 8),
    ESTRELA("Estrela Savage", CollectibleRarity.EPIC, Material.NETHER_STAR, (short) 0, 8),
    BONE("Osso do Pet", CollectibleRarity.COMUM, Material.BONE, (short) 0, 8),
    COMPASSO("Bússola VIP", CollectibleRarity.EPIC, Material.COMPASS, (short) 0, 8),
    MAÇA("Maçã Dourada", CollectibleRarity.MYTHICAL, Material.GOLDEN_APPLE, (short) 0, 8);

    private final GadgetCollectible cosmetic;

    GadgetList(String name, CollectibleRarity rarity, Material material, short data, int slot) {
        this.cosmetic = new GadgetCollectible(name, rarity, Arrays.asList(RankType.MEMBER, RankType.VIP),
                material, data, slot, System.currentTimeMillis());
        cosmetic.setLore(Arrays.asList("§7Item decorativo no hotbar.", "", "§7Raridade: " + rarity.getColoredName()));
    }
}
