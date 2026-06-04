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

public class VinteDoisMeuVulgoTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /** Bordos §8✦ §6✦ — trechos §6§l / §8§l (Oruam · Liberdade). */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§8§l✦ §6§l22 meu vulgo §8§l✦",
            "§6§l✦ §8§lJá pensei em parar §6§l✦",
            "§8§l✦ §6§lCaí e me levantei §8§l✦",
            "§6§l✦ §8§lTô na luta do foco §6§l✦",
            "§8§l✦ §6§lConfesso, não gosto de falhar §8§l✦",
            "§6§l✦ §8§lMeus anjos e demônios me deixaram de lado §6§l✦",
            "§8§l✦ §6§lJuro que eu não gosto de chorar §8§l✦",
            "§6§l✦ §8§lSobrecarregado com o peso da glória §6§l✦",
            "§8§l✦ §6§lÉ que o homem era só um menino §8§l✦",
            "§6§l✦ §8§lE todas cicatrizes marcam sua trajetória §6§l✦",
            "§8§l✦ §6§lOuro no pescoço, também ouro no dente §8§l✦",
            "§6§l✦ §8§lTerror dos invejoso, Jesus no pingente §6§l✦",
            "§8§l✦ §6§l22 meu vulgo, pra tu ver que eu sou maluco §8§l✦",
            "§6§l✦ §8§lCara de ódio pra mostrar que eu tô puto §6§l✦",
            "§8§l✦ §6§lNão rendo pra ninguém, problema do Estado §8§l✦",
            "§6§l✦ §8§lMontei meu castelo, botei medo no Diabo §6§l✦",
            "§8§l✦ §6§lHumildade pra vencer, chegar onde tu quiser §8§l✦",
            "§6§l✦ §8§lNovin rei das mulher, sempre destacado §6§l✦",
            "§8§l✦ §6§lAtacante brabo, pique Cristiano Ronaldo §8§l✦",
            "§6§l✦ §8§lQuem nunca sentou no banco dos réus §6§l✦",
            "§8§l✦ §6§lGanhei esse jogo e joguei fora o troféu §8§l✦",
            "§6§l✦ §8§l22 meu vulgo §6§l✦"
    ));

    public VinteDoisMeuVulgoTitle() {
        super("22 meu vulgo", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§8§l✦ §6§l22 meu vulgo §8§l✦",
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
