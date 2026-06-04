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

public class CafajesteChucroTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Bordos §6◈ / §e◈; letra §e§l / §6§l — clima terra / cafajeste.
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§6§l◈ §e§lCafajeste Chucro §6§l◈",
            "§e§l◈ §6§lCafajeste, xucro, sempre louco e embrazado §e§l◈",
            "§6§l◈ §e§lUm cara matuto, mas também sou brincalhão §6§l◈",
            "§e§l◈ §6§lÔ, gatinha, eu juro, não foi minha intenção §e§l◈",
            "§6§l◈ §e§lDe te machucar nem brincar com o coração §6§l◈",
            "§e§l◈ §6§lPra fiel, dei um apê e, pra amante, um carro novo §e§l◈",
            "§6§l◈ §e§lPras da rua, eu dou tetê, eu sou um cara generoso §6§l◈",
            "§e§l◈ §6§lÉ meu jeito de viver e sei que ela não gosta disso §e§l◈",
            "§6§l◈ §e§lMinha bebê não quer voltar nem quer ver outra comigo §6§l◈",
            "§e§l◈ §6§lCafajeste Chucro §e§l◈"
    ));

    public CafajesteChucroTitle() {
        super("Cafajeste Chucro", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§6§l◈ §e§lCafajeste Chucro §6§l◈",
                "",
                "§bAura §8· §7Trecho animado §e§6",
                "",
                "§8Raridade §5Mítica"
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
