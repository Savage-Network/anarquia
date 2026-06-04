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

public class ArtistaGenericoTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /** §7★ §f★ — gancho §f§l / §8§o (Dos Prédio / Veigh). */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§7§l★ §f§lArtista genérico §7§l★",
            "§f§l★ §8§oBaby, é que eu vim dos prédio e eu conheço a rua §f§l★",
            "§7§l★ §f§lEu sou real, esse mano simula §7§l★",
            "§f§l★ §8§oNóis fez um império §f§l★",
            "§7§l★ §f§lEu fui o primeiro a vencer do meu bairro §7§l★",
            "§f§l★ §8§oPostou que eu sou um artista genérico §f§l★",
            "§7§l★ §f§lEscrever nas redes tem sido tão fácil §7§l★",
            "§f§l★ §8§oEnquanto ele odeia, nóis realiza §f§l★",
            "§7§l★ §f§lPeço que obtenha a visão de mercado §7§l★",
            "§f§l★ §8§oComo eu vou dar brecha pro inimigo §f§l★",
            "§7§l★ §f§lse, quando eu oro, eu falo em voz baixa? §7§l★",
            "§f§l★ §8§oCarro Aston Martin, mente cem por cento habilidade §f§l★",
            "§7§l★ §f§lDos Prédio on, nunca tá off-line §7§l★",
            "§f§l★ §f§oArtista genérico §f§l★"
    ));

    public ArtistaGenericoTitle() {
        super("Artista Generico", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7§l★ §f§lArtista genérico §7§l★",
                "",
                "§7Trecho animado §7§f§8 §7· Veigh",
                "",
                "§8Raridade §5Mítica"
        ));
    }

    @Override
    public CollectibleMusicArtist getMusicArtist() {
        return CollectibleMusicArtist.VEIGH;
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
