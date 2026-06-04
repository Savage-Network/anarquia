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

public class MePostouNoDailyMcGpTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Bordos §c◈ / §8◈; letra §c§l / §4§l (rua / “daily”).
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§c§l◈ §8§lMe Postou no daily §7– §fMC GP §c§l◈",
            "§8§l◈ §4§lNão me iludo no: Te amo, pego firme essa maldita §8§l◈",
            "§c§l◈ §4§lSalceirando pela rua, bagunçando as avenida §c§l◈",
            "§8§l◈ §4§lJoga a Tiger lá em cima, ela ama adrenalina §8§l◈",
            "§c§l◈ §4§lGuarda minha 9 milímetro na sua bolsa bonita §c§l◈",
            "§8§l◈ §4§lQuer sensação de poder, piranha do lazer §8§l◈",
            "§c§l◈ §4§lQuer lipo HD, trinta mil na tetê §c§l◈",
            "§8§l◈ §4§lFesta do Big G, bala, doce, MD §8§l◈",
            "§c§l◈ §4§lPutinha quer prazer sem se comprometer §c§l◈",
            "§8§l◈ §4§lSenta, senta, senta pro patrão §7(senta, senta) §8§l◈",
            "§c§l◈ §4§lMostra que tu tem disposição §7(desce, desce) §c§l◈",
            "§8§l◈ §4§lEla que já nasceu com esse dom §7(bandida) §8§l◈",
            "§c§l◈ §4§lDe levar coração de ladrão §7(vai, vai, vai) §c§l◈",
            "§8§l◈ §4§lSenta, senta, senta pro patrão §7(senta, senta) §8§l◈",
            "§c§l◈ §4§lMostra que tu tem disposição §7(desce, desce) §c§l◈",
            "§8§l◈ §4§lEla que já nasceu com esse dom §7(bandida) §8§l◈",
            "§c§l◈ §4§lDe levar coração de ladrão §7(vai, vai, vai) §c§l◈",
            "§8§l◈ §4§lMe Postou no daily §7(MC GP) §8§l◈"
    ));

    public MePostouNoDailyMcGpTitle() {
        super("Me Postou no daily - MC GP", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§c§l◈ §8§lMe Postou no daily §7– §fMC GP §c§l◈",
                "",
                "§7Trecho animado §c§8 §7/ §fMC GP",
                "",
                "§8Raridade §5Mítica"
        ));
    }

    @Override
    public CollectibleMusicArtist getMusicArtist() {
        return CollectibleMusicArtist.MC_GP;
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
