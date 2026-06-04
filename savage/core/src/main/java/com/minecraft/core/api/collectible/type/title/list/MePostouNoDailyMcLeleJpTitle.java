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

public class MePostouNoDailyMcLeleJpTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Bordos §5♦ / §d♦; letra §d§l / §5§l — variante §7MC Lele JP§r (distinct do MC GP em vermelho).
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§5§l♦ §d§lMe Postou no daily §7– §fMC Lele JP §5§l♦",
            "§d§l♦ §5§lTá chovendo puta na festa do Big G §d§l♦",
            "§5§l♦ §d§lSó cavala pra marchar, só pegar e escolher §5§l♦",
            "§d§l♦ §5§lDogDog manda as pura, quero ver bundão descer §d§l♦",
            "§5§l♦ §d§lTem lança pra baforar, nóis que faz acontecer §5§l♦",
            "§d§l♦ §5§lBebê, te trombei na Vilma, toda safadinha §d§l♦",
            "§5§l♦ §d§lPediu pra montar na garupa do drake §5§l♦",
            "§d§l♦ §5§lSó menor quentão na pista, de nove na cinta §d§l♦",
            "§5§l♦ §d§lEla ganhou de lá que é o chatão que tá §5§l♦",
            "§d§l♦ §5§lVai sarrar na peça, sua sapeca, desestressa §d§l♦",
            "§5§l♦ §d§lMe postou no daily pra se achar pras colega §5§l♦",
            "§d§l♦ §5§lTomou de sequência, a cavala tá sem sela §d§l♦",
            "§5§l♦ §d§lColoquei na chapa, quem mandou vir na minha reta? §5§l♦",
            "§d§l♦ §5§lVai sarrar na peça, sua sapeca, desestressa §d§l♦",
            "§5§l♦ §d§lMe postou no daily pra se achar pras colega §5§l♦",
            "§d§l♦ §5§lTomou de sequência, a cavala tá sem sela §d§l♦",
            "§5§l♦ §d§lColoquei na chapa, quem mandou vir na minha reta? §5§l♦",
            "§d§l♦ §5§lMe Postou no daily §7(MC Lele JP) §d§l♦"
    ));

    public MePostouNoDailyMcLeleJpTitle() {
        super("Me Postou no daily - MC Lele JP", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§5§l♦ §d§lMe Postou no daily §7– §fMC Lele JP §5§l♦",
                "",
                "§7Trecho animado §d§5 §7/ §fMC Lele JP",
                "",
                "§8Raridade §5Mítica"
        ));
    }

    @Override
    public CollectibleMusicArtist getMusicArtist() {
        return CollectibleMusicArtist.MC_LELE_JP;
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
