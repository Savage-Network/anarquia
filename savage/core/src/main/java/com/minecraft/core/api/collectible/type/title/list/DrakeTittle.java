package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.CollectibleTitleGroup;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DrakeTittle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Frames da letra animada / cores seqüenciais.
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§1§lVivi PKRL na mão dos DRAKE",
            "§2§lGente Vocês não tem noção",
            "§3§lEu comecei a andar com umas menina",
            "§4§lQue é super arrastora",
            "§5§lMas elas são a mesma fita",
            "§6§lVivi PKRL na mão dos DRAKE",
            "§7§lGente Vocês não tem noção",
            "§8§lEu comecei a andar com umas menina",
            "§9§lQue é super arrastora",
            "§a§lMas elas são a mesma fita"
    ));

    public DrakeTittle() {
        super("Vivi na mão dos DRAKE", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§b§lVivi na mão dos DRAKE.",
                "",
                "§8Raridade §5Mítica§8 · §7cicla §80–9§7 e §fa–f"
        ));
    }

    @Override
    public CollectibleTitleGroup getTitleGroup() {
        return CollectibleTitleGroup.AURA;
    }

    @Override
    public String getTitle() {
        return ANIMATION_FRAMES.get(0);
    }

    @Override
    public List<String> getTitleAnimationFrames() {
        return ANIMATION_FRAMES;
    }

    @Override
    public int getTitleAnimationStepsPerFrame() {
        return SLOW_ANIMATION_STEPS_PER_FRAME;
    }
}
