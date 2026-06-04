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

public class PenelopeCharmosaTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Bordos §5❀ / §d❀; verso §5§l / §d§l alternando (cores &5 / &d no texto central).
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§5❀ §d§lEla é bonitinha §5❀",
            "§d❀ §5§lEla tá querendo §d❀",
            "§5❀ §d§lEla faz carinha §5❀",
            "§d❀ §5§lE joga o cabelo §d❀",
            "§5❀ §d§lPenélope Charmosa §5❀",
            "§d❀ §5§lJoga pra mim, mas tem que ser de costa §d❀",
            "§5❀ §d§lPenélope Charmosa §5❀",
            "§d❀ §5§lEmbrazadinha, não quer mais ir embora (pega) §d❀",
            "§5❀ §d§lPenélope Charmosa §5❀",
            "§d❀ §5§lSentou pros cria e gostou na onda §d❀"
    ));

    public PenelopeCharmosaTitle() {
        super("Penélope Charmosa", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§5❀ §d§lPenélope Charmosa §5❀",
                "",
                "§7Trecho animado §5❀ §d❀ §7/ letra §5§d",
                "",
                "§8Raridade §5Mítica"
        ));
    }

    @Override
    public CollectibleMusicArtist getMusicArtist() {
        return CollectibleMusicArtist.BOLADIN211;
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
