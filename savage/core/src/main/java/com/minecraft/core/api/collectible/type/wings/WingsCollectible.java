package com.minecraft.core.api.collectible.type.wings;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.item.Item;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class WingsCollectible extends Collectible {

    private final List<String> frames = new ArrayList<>();
    private int ticksPerFrame = 3;

    public WingsCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, long releasedAt) {
        super(name, CollectibleCategory.WINGS, rarity, ranks, releasedAt);
        setIcon(Item.of(Material.FEATHER));
        Core.getCollectibleController().save(this);
    }

    public void addFrames(String... values) {
        frames.addAll(Arrays.asList(values));
        if (getHeadValue() == null && !frames.isEmpty())
            setHeadValue(frames.get(0));
    }

    public static void handle() {
        for (WingsList wing : WingsList.values())
            Core.getCollectibleController().save(wing.getCosmetic());
    }
}
