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

public class MitsubishiTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /** Bordos §4◆ §c◆ — trechos §c§l / §4§l (Orochi). */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§4§l◆ §c§lMitsubishi §4§l◆",
            "§c§l◆ §4§lPego o dinheiro §c§l◆",
            "§4§l◆ §c§lJogo na bitch §4§l◆",
            "§c§l◆ §4§lNa previsão, hoje a chuva é de uísque §c§l◆",
            "§4§l◆ §c§lOntem eu não tinha grana pra tá na suíte §4§l◆",
            "§c§l◆ §4§lHoje eu paguei à vista uma Mitsubishi §c§l◆",
            "§4§l◆ §c§lJuro que eu não ligo pra quem fala de mim §4§l◆",
            "§c§l◆ §4§lSó ligo pro meu PG no final do mês §c§l◆",
            "§4§l◆ §c§lEsses arrombados querem me ver cair §4§l◆",
            "§c§l◆ §4§lMas eu sempre mato todos de uma só vez §c§l◆",
            "§4§l◆ §c§lIgual no filme, minha bala faz curva e acerta o Matrix §4§l◆",
            "§c§l◆ §4§lA mente mais cara do mundo dos feat §c§l◆",
            "§4§l◆ §c§lEu ando de gangue, nós manda na city §4§l◆",
            "§c§l◆ §4§lGanhando e gastando nóis prefere assim §c§l◆",
            "§4§l◆ §c§lEu mato por cada mano do meu bonde §4§l◆",
            "§c§l◆ §4§lÉ certo que eles fariam o mesmo por mim §c§l◆",
            "§4§l◆ §c§lMitsubishi §4§l◆"
    ));

    public MitsubishiTitle() {
        super("Mitsubishi", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§4§l◆ §c§lMitsubishi §4§l◆",
                "",
                "§7Trecho animado §4§c §7· Orochi",
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
