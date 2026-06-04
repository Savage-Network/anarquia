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

public class X1Title extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Bordos §6♫ / §e♫; verso §e§l / §6§l alternando (equivalente a &e/&6 pedido).
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§6♫ §e§lEla sabe quem é o Little Hair §6♫",
            "§e♫ §6§lDiz aí quem não sabe, né? §e♫",
            "§6♫ §e§lDuvido que não sabe §6♫",
            "§e♫ §6§lFala a verdade, que fiz com jeitinho e é isso que te fez ficar §e♫",
            "§6♫ §e§lTu fez melhorar meus tempos de crise §6♫",
            "§e♫ §6§lDo seu lado eu tô chique §e♫",
            "§6♫ §e§lMeu cachê tá valendo, sim, um preço de um Volvo §6♫",
            "§e♫ §6§lInvestindo meus bens, empilhando ouro §e♫",
            "§6♫ §e§lMe sinto tão bem, bonito e gostoso §6♫",
            "§e♫ §6§lTô rico também, favela no topo §e♫",
            "§6♫ §e§lQuem te falou que eu não ia chegar? §6♫",
            "§e♫ §6§lQuem falou que eu não ia vencer? §e♫",
            "§6♫ §e§lAinda bem, não parei de sonhar §6♫",
            "§e♫ §6§lPapai do céu, só tenho a agradecer §e♫",
            "§6♫ §e§lHoje eles chora §6♫",
            "§e♫ §6§lPorque toda hora vai me ver fazendo hit §e♫",
            "§6♫ §e§lCabelinho acelera na pista, a nave faz vroom §6♫"
    ));

    public X1Title() {
        super("X1", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§6♫ §e§lLittle Hair — X1 §6♫",
                "",
                "§7Trecho animado §6♫ §e♫ §7/ letra §e§6",
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
