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

public class TripleSevenTripleSixTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Bordos §5✦ / §c✦; letra §c§l / §5§l (mistério / “demônio” na letra).
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§5§l✦ §c§l777-666 §5§l✦",
            "§c§l✦ §5§lQuando ela bater §c§l✦",
            "§5§l✦ §c§lCura ela vai te levar §5§l✦",
            "§c§l✦ §5§lVai mais um, é melhor esperar §c§l✦",
            "§5§l✦ §c§lQue quando a onda bater §5§l✦",
            "§c§l✦ §5§lTu vai ter que aprender a surfar §c§l✦",
            "§5§l✦ §c§lSubi demais, não dá mais pra voltar §5§l✦",
            "§c§l✦ §5§lNunca mais eu vou voltar §c§l✦",
            "§5§l✦ §c§lVou me mudar para lá §5§l✦",
            "§c§l✦ §5§l§7(Alguém tira ele de lá) §c§l✦",
            "§5§l✦ §c§lEu tô num loop vivendo coisas que eu já vivi §5§l✦",
            "§c§l✦ §5§lDizendo coisas que eu já disse §c§l✦",
            "§5§l✦ §c§lVivendo a eterna bad trip §5§l✦",
            "§c§l✦ §5§lAndam dizendo que o Tuê é o demônio §c§l✦",
            "§5§l✦ §c§lTudo que ele escreve te consome, ayy §5§l✦",
            "§c§l✦ §5§lNão vai se deixar levar pelo som §c§l✦",
            "§5§l✦ §c§lÉ no detalhe que o diabo se esconde, ayy §5§l✦",
            "§c§l✦ §5§l777-666 §c§l✦"
    ));

    public TripleSevenTripleSixTitle() {
        super("777-666", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§5§l✦ §c§l777-666 §5§l✦",
                "",
                "§7Trecho animado §5§c §7/ Tuê",
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
