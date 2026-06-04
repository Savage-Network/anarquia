package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.CollectibleMusicArtist;
import com.minecraft.core.api.collectible.type.title.CollectibleTitleGroup;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VanCleefTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Bordos §6⋆ / §b⋆; letra alternando §b§l / §6§l (ouro + água — Van Cleef / suíte à beira-mar).
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§6§l⋆ §b§lOh-oh-oh-oh-ahn, yeah §7(Orochi) §6§l⋆",
            "§b§l⋆ §6§lTroca de energia §b§l⋆",
            "§6§l⋆ §b§lDepois daquela última noite, da minha mente tu não sai por nada §6§l⋆",
            "§b§l⋆ §6§lNossas fotos antigas §b§l⋆",
            "§6§l⋆ §b§lAinda tão na minha galera memo nóis não dando certo, gata §6§l⋆",
            "§b§l⋆ §6§lTe comprei Van Cleef, tratamento VIP §b§l⋆",
            "§6§l⋆ §b§lA melhor suíte, com a vista da praia §6§l⋆",
            "§b§l⋆ §6§lLiga o Netflix, deita pra ver um filme §b§l⋆",
            "§6§l⋆ §b§lSeparei daquela espécie que sempre te atrai §6§l⋆",
            "§b§l⋆ §6§lVan Cleef §b§l⋆"
    ));

    public VanCleefTitle() {
        super("Van Cleef", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§6§l⋆ §b§lVan Cleef §6§l⋆",
                "",
                "§7Trecho animado §6§b§7 / Orochi",
                "",
                "§8Raridade §5Mítica"
        ));
    }

    @Override
    public CollectibleMusicArtist getMusicArtist() {
        return CollectibleMusicArtist.OROCHI;
    }

    @Override
    public CollectibleTitleGroup getTitleGroup() {
        return CollectibleTitleGroup.MUSICAS;
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
