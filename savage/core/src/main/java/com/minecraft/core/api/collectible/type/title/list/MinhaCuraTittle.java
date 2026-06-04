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

public class MinhaCuraTittle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Frames do título troca vermelho / alternância §4§c.
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§4❤ §c§lVocê é minha cura §4❤",
            "§c❤ §4§lJura que tu não vai me abandonar? §c❤",
            "§4❤ §c§lMe fazendo perder a postura de cria §4❤",
            "§c❤ §4§lAdmito, não vou negar §c❤",
            "§4❤ §c§lNão vou dar esse gosto quе esse povo quer §4❤",
            "§c❤ §4§lTipo mе ver vacilando com a minha mulher §c❤",
            "§4❤ §c§lVários cara sente inveja, querem meu lugar §4❤",
            "§c❤ §4§lVárias mina me deseja, mas só pode olhar §c❤"
    ));

    public MinhaCuraTittle() {
        super("MinhaCura", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§c§lVocê é minha cura.",
                "",
                "§4§L❤ §CDEDICADO PARA APAIXONADOS §4§L❤",
                "",
                "§8Raridade §5Mítica"
        ));
    }

    @Override
    public CollectibleMusicArtist getMusicArtist() {
        return CollectibleMusicArtist.CABELINHO;
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
