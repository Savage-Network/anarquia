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

public class AkDeBipeTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /** Bordos §8✦ §6✦ — trechos §6§l / §8§l (Oruam · Liberdade). */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§8§l✦ §6§lAK de Bipé §8§l✦",
            "§6§l✦ §8§lFaz o que você quiser §6§l✦",
            "§8§l✦ §6§lSó não encosta no meu boot que esse aqui é novo §8§l✦",
            "§6§l✦ §8§lSempre tô driblando todos invejosos §6§l✦",
            "§8§l✦ §6§lSempre tá do lado babando meu ovo §8§l✦",
            "§6§l✦ §8§lPara de falar da minha vida §6§l✦",
            "§8§l✦ §6§lTá de fofoquinha com a porra das tuas amigas §8§l✦",
            "§6§l✦ §8§lAK de Bipé §6§l✦"
    ));

    public AkDeBipeTitle() {
        super("AK de Bipé", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§8§l✦ §6§lAK de Bipé §8§l✦",
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
