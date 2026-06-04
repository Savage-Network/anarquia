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

public class LeiAntiOruamTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /** Bordos §8✦ §6✦ — trechos §6§l / §8§l (Oruam · Liberdade). */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§8§l✦ §6§lLei ANTI ORUAM §8§l✦",
            "§6§l✦ §8§lOi, boladão, pesadão, não conto com a sorte §6§l✦",
            "§8§l✦ §6§lMinha Glock travou no Robocop §8§l✦",
            "§6§l✦ §8§lO tráfico tá virando esporte §6§l✦",
            "§8§l✦ §6§lFormou foi mó complexão §8§l✦",
            "§6§l✦ §8§lMas o que falta é educação §6§l✦",
            "§8§l✦ §6§lUm dia que o fuzil e a pistola §8§l✦",
            "§6§l✦ §8§lValer mais que um livro §6§l✦",
            "§8§l✦ §6§lAí tem algo errado §8§l✦",
            "§6§l✦ §8§lEles dão arma pra nóis, depois pergunta por que somo bandido §6§l✦",
            "§8§l✦ §6§lVou me expressar §8§l✦",
            "§6§l✦ §8§lTalvez o meu tempo acabe §6§l✦",
            "§8§l✦ §6§lO mundo não cabe os troféus que eu ganhei enfrentando batalhas §8§l✦",
            "§6§l✦ §8§lTudo na vida são fases §6§l✦",
            "§8§l✦ §6§lPor isso, ódio nas frases §8§l✦",
            "§6§l✦ §8§lExplica pra uma criança por que seu herói vive dentro das grades §6§l✦",
            "§8§l✦ §6§lÉ que eu não era ruim, mas o mundo me fez assim §8§l✦",
            "§6§l✦ §8§lJulga, mas não vê o mundo que eu vejo §6§l✦",
            "§8§l✦ §6§lAprendi o segredo da vida, o dinheiro e o poder §8§l✦",
            "§6§l✦ §8§lPrefiro o sorriso sincero §6§l✦",
            "§8§l✦ §6§lNo fundo, eu sei que tudo é ego §8§l✦",
            "§6§l✦ §8§lDiz que é fã, quer me dar — não sabe diferenciar §6§l✦",
            "§8§l✦ §6§lO Mauro do Oruam §8§l✦",
            "§6§l✦ §8§lÉ que o trap sugou minha alma §6§l✦",
            "§8§l✦ §6§lLei ANTI ORUAM §8§l✦"
    ));

    public LeiAntiOruamTitle() {
        super("Lei ANTI ORUAM", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§8§l✦ §6§lLei ANTI ORUAM §8§l✦",
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
