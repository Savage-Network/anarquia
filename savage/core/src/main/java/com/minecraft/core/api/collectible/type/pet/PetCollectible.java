package com.minecraft.core.api.collectible.type.pet;

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
public class PetCollectible extends Collectible {

    private List<String> frames = new ArrayList<>();
    private int ticksPerFrame = 4;

    public PetCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, long releasedAt) {
        super(name, CollectibleCategory.PET, rarity, ranks, releasedAt);
        setIcon(Item.of(Material.BONE));
        Core.getCollectibleController().save(this);
    }

    public void addFrames(String... values) {
        frames.addAll(Arrays.asList(values));
        if (getHeadValue() == null && !frames.isEmpty())
            setHeadValue(frames.get(0));
    }

    public static void handle() {
        for (PetList pet : PetList.values())
            Core.getCollectibleController().save(pet.getCosmetic());
    }
}
