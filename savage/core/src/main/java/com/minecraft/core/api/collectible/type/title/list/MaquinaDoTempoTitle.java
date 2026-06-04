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

public class MaquinaDoTempoTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /** Bordos §5⚡ §d⚡ · trecho §d§l / §5§l (Matuê). */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§5§l⚡ §d§lMáquina do Tempo §5§l⚡",
            "§d§l⚡ §5§lMe vê: Duplo Rolex, fumando um becks §d§l⚡",
            "§5§l⚡ §d§lHoje eu trouxe um slick cheio do toprê §5§l⚡",
            "§d§l⚡ §5§lDerrubo um Jack, bola uma track §d§l⚡",
            "§5§l⚡ §d§lSempre existe mais dinheiro a fazer §5§l⚡",
            "§d§l⚡ §5§lUh, ayy, dinheiro a fazer §d§l⚡",
            "§5§l⚡ §d§lAhn, domingão queimando gasolina §5§l⚡",
            "§d§l⚡ §5§l(Domingão queimando gasolina) §d§l⚡",
            "§5§l⚡ §d§lFoi mal por comer a sua prima §5§l⚡",
            "§d§l⚡ §5§lMas a bunda dela me fascina, yah §d§l⚡",
            "§5§l⚡ §d§lMas a bunda dela me fasc §5§l⚡",
            "§d§l⚡ §5§lEu vou fazer uma máquina do tempo §d§l⚡",
            "§5§l⚡ §d§lEncher ela de boldo §5§l⚡",
            "§d§l⚡ §5§lVoltar pro passado e escrever tudo de novo §d§l⚡",
            "§5§l⚡ §d§lVou pros anos '70, encontrar com o meu sogro §5§l⚡",
            "§d§l⚡ §5§lBaseado mais bolado que o outro §d§l⚡",
            "§5§l⚡ §d§lMáquina do Tempo §5§l⚡"
    ));

    public MaquinaDoTempoTitle() {
        super("Maquina do Tempo", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§5§l⚡ §d§lMáquina do Tempo §5§l⚡",
                "",
                "§7Trecho animado §5§d §7· Matuê",
                "",
                "§8Raridade §5Mítica"
        ));
    }

    @Override
    public CollectibleMusicArtist getMusicArtist() {
        return CollectibleMusicArtist.MATUE;
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
