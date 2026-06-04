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

public class TermineiRecentementeTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Frames com alternância §6§l℧ / §e§l℧ nos cantos e letra §e§l / §6§l.
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§6§l℧ §e§l(Vinicin) §6§l℧",
            "§e§l℧ §6§lEu nem queria atender sua ligação §e§l℧",
            "§6§l℧ §e§lMas também tava com tesão §6§l℧",
            "§e§l℧ §6§lGata, eu pensei em te falar §e§l℧",
            "§6§l℧ §e§lQue agora nós já tá no jogo de pressão §6§l℧",
            "§e§l℧ §6§lTu não vai ter meu coração §e§l℧",
            "§6§l℧ §e§lNinguém vai mais me conquistar §6§l℧",
            "§e§l℧ §6§lEu tô de pinote, levando na solidão §e§l℧",
            "§6§l℧ §e§lBaby, eu sou a solução pra minha vida melhorar §6§l℧",
            "§e§l℧ §6§lE hoje eu só viso a nave de milhão §e§l℧",
            "§6§l℧ §e§lTô vivendo do humildão, só Deus pode me parar §6§l℧",
            "§e§l℧ §6§lDá mais um trago, fica de quatro §e§l℧",
            "§6§l℧ §e§lBebe comigo essa garrafa de uísque §6§l℧"
    ));

    public TermineiRecentementeTitle() {
        super("Terminei Recentemente", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§6§l℧ §e§lTerminei recentemente. §6§l℧",
                "",
                "§7Trecho animado em §e§6",
                "",
                "§8Raridade §5Mítica"
        ));
    }

    @Override
    public CollectibleMusicArtist getMusicArtist() {
        return CollectibleMusicArtist.VINICIN;
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
