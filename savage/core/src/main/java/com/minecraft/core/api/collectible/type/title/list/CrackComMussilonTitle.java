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

public class CrackComMussilonTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Bordos §6◆ / §e◆; letra §e§l / §6§l (ouro / drip “luxo & moda”).
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§6§l◆ §e§lCrack com Mussilon §6§l◆",
            "§e§l◆ §6§lRosê no braço, meu mano, eu sou pardo, por isso eu só fumo marrom §e§l◆",
            "§6§l◆ §e§lNóis não sai do tom §7(não)§e§l, a bad nóis tira no som, ei §6§l◆",
            "§e§l◆ §6§lEu tô protegido com pente estendido na bolsa da Louis Vuitton §7(Louis) §e§l◆",
            "§6§l◆ §e§lYves Saint Laurent, pi-pi-piso de Lanvin §7(yeah) §6§l◆",
            "§e§l◆ §6§lTava na rua, servindo essa droga, vivendo que nem um garçom §e§l◆",
            "§6§l◆ §e§lMisturo crack com Mussilon §7(yeah, yeah) §6§l◆",
            "§e§l◆ §6§lFazer dinheiro eu sei que eu tenho o dom §e§l◆",
            "§6§l◆ §e§lEla vem sendo vadia já faz alguns dias, sujou minha calça de batom §6§l◆",
            "§e§l◆ §6§lEsses fudido eu nem sei quem são §e§l◆",
            "§6§l◆ §e§lÉ que não dá pra andar com vacilão §6§l◆",
            "§e§l◆ §6§lVários estilos, jeito que eu me visto, meu drip no estilo, sou o mais fashion §e§l◆",
            "§6§l◆ §e§lEla lançou logo os peito e a raba no vídeo só pra pegar a minha reaction §6§l◆",
            "§e§l◆ §6§lMinha bitch é mais fashion, a minha bitch é mais fashion §e§l◆",
            "§6§l◆ §e§lMinha nina tá comigo, se ela te dar um grito, nego, não se mete §7(não) §6§l◆",
            "§e§l◆ §6§lCrack com Mussilon §e§l◆"
    ));

    public CrackComMussilonTitle() {
        super("Crack com Mussilon", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§6§l◆ §e§lCrack com Mussilon §6§l◆",
                "",
                "§7Trecho animado §e§6",
                "",
                "§8Raridade §5Mítica"
        ));
    }

    @Override
    public CollectibleMusicArtist getMusicArtist() {
        return CollectibleMusicArtist.MATUE;
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
