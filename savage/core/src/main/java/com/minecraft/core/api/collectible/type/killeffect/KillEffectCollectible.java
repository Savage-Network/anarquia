package com.minecraft.core.api.collectible.type.killeffect;

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
public class KillEffectCollectible extends Collectible {

    private final Effect effect;
    private final int amount;

    public KillEffectCollectible(String name, CollectibleRarity rarity, List<RankType> ranks,
                                 Effect effect, int amount, Material icon, long releasedAt) {
        super(name, CollectibleCategory.KILL_EFFECT, rarity, ranks, releasedAt);
        this.effect = effect;
        this.amount = amount;
        setIcon(Item.of(icon));
        Core.getCollectibleController().save(this);
    }

    public static void handle() {
        for (KillEffectList fx : KillEffectList.values())
            Core.getCollectibleController().save(fx.getCosmetic());
    }
}
