package com.minecraft.core.api.collectible.type.mutation;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import org.bukkit.Effect;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

@Getter
public class MutationCollectible extends Collectible {

    private final Effect effect;
    private final int particleAmount;
    private final boolean glow;

    public MutationCollectible(String name, CollectibleRarity rarity, List<RankType> ranks,
                               Effect effect, int particleAmount, boolean glow, Material icon, long releasedAt) {
        super(name, CollectibleCategory.MUTATION, rarity, ranks, releasedAt);
        this.effect = effect;
        this.particleAmount = particleAmount;
        this.glow = glow;
        setIcon(Item.of(icon));
        Core.getCollectibleController().save(this);
    }

    public static void handle() {
        for (MutationList mutation : MutationList.values())
            Core.getCollectibleController().save(mutation.getCosmetic());
    }
}
