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

public class NaoMentePraMentirosoTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /** Bordos §8✦ §6✦ — trechos §6§l / §8§l (Oruam · Liberdade). */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§8§l✦ §6§lNão mente pra mentiroso §8§l✦",
            "§6§l✦ §8§lCê quer o mundo §6§l✦",
            "§8§l✦ §6§lE eu te dei de tudo §8§l✦",
            "§6§l✦ §8§lE sei que não foi o suficiente §6§l✦",
            "§8§l✦ §6§lSei que não pensou na gente §8§l✦",
            "§6§l✦ §8§lMenina louca §6§l✦",
            "§8§l✦ §6§lEu vi cê tirar sua roupa §8§l✦",
            "§6§l✦ §8§lE bagunçou a porra toda §6§l✦",
            "§8§l✦ §6§lEu te vesti de Gucci, de Fendi, de Prada §8§l✦",
            "§6§l✦ §8§lMas eu prefiro é te ver pelada §6§l✦",
            "§8§l✦ §6§lLá em casa, safada §8§l✦",
            "§6§l✦ §8§lVocê falou que nunca mais ia voltar §6§l✦",
            "§8§l✦ §6§lMas cê levou as chaves §8§l✦",
            "§6§l✦ §8§lCê deixou sua roupa §6§l✦",
            "§8§l✦ §6§lCê morou na minha mente §8§l✦",
            "§6§l✦ §8§lBagunçou a porra toda §6§l✦",
            "§8§l✦ §6§lNão mente pra mentiroso §8§l✦"
    ));

    public NaoMentePraMentirosoTitle() {
        super("Não mente pra mentiroso", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§8§l✦ §6§lNão mente pra mentiroso §8§l✦",
                "",
                "§7Trecho animado §8§6 §7· Oruam — Liberdade",
                "",
                "§8Raridade §5Mítica"
        ));
    }

    @Override
    public CollectibleMusicArtist getMusicArtist() {
        return CollectibleMusicArtist.ORUAM;
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
