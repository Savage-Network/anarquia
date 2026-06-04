package com.minecraft.core.api.collectible.type.title;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;

import java.util.Collections;
import java.util.List;

public abstract class TitleCollectible extends Collectible {

    public TitleCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, long releasedAt) {
        super(name, CollectibleCategory.TITLE, rarity, ranks, releasedAt);
    }

    /**
     * Subcategoria usada no filtro do menu de títulos.
     */
    public CollectibleTitleGroup getTitleGroup() {
        return CollectibleTitleGroup.PROFILE_STATS;
    }

    /**
     * Interpretação / MC do título quando {@link #getTitleGroup()} é {@link CollectibleTitleGroup#MUSICAS}.
     * Para classificar músicas novas até saberes o artista, deixa-se o default {@link CollectibleMusicArtist#UNKNOWN}.
     */
    public CollectibleMusicArtist getMusicArtist() {
        return CollectibleMusicArtist.UNKNOWN;
    }
    
    /**
     * Retorna o título a ser exibido
     */
    public abstract String getTitle();
    
    /**
     * Retorna o subtítulo a ser exibido (pode ser null)
     */
    public String getSubtitle() {
        return null;
    }

    /**
     * Quantas atualizações do holograma do título (~10 ticks cada em Bukkit) até avançar um quadro animado.
     * 1 = velocidade padrão; valores maiores = animação mais lenta.
     */
    public int getTitleAnimationStepsPerFrame() {
        return 1;
    }

    /**
     * Quadros do título acima do nome, em ordem (vazio = usa só {@link #getTitle()}).
     */
    public List<String> getTitleAnimationFrames() {
        return Collections.emptyList();
    }
}
