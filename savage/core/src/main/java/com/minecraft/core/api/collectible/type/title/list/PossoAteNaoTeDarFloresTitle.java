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

public class PossoAteNaoTeDarFloresTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Bordos §5✿ / §d✿; verso §d§l / §5§l alternando (equivalente a &d/&5 pedido).
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§5✿ §d§lBebê, eu não nego que você me fez feliz §5✿",
            "§d✿ §5§lMas um findi sem copão o coração fica triste §d✿",
            "§5✿ §d§lPerdi noite de bailão, pelo date, dormi cedo §5✿",
            "§d✿ §5§lMe joguei na contramão §d✿",
            "§5✿ §d§lVou pa rua com os parceiro §5✿",
            "§d✿ §5§lSe pa tu tá bom, pa mim também §d✿",
            "§5✿ §d§lMelhor coisa ficar solteiro §5✿",
            "§d✿ §5§lEsperei chegar o verão, tirei a aliança do dedo §d✿",
            "§5✿ §d§lSe pa tu tá bom, pa mim também §5✿",
            "§d✿ §5§lMelhor coisa é ficar solteiro §d✿",
            "§5✿ §d§lAhn, nóis tá vivendo tudo que era desejo §5✿",
            "§d✿ §5§lSenta, poca §d✿",
            "§5✿ §d§lToda peca dela, vai querer ficar §5✿",
            "§d✿ §5§lDogDog tá de peça, Japa NK §d✿",
            "§5✿ §d§lBeat que te deixa lenta, ah §5✿",
            "§d✿ §5§lEla senta, ela quica §d✿",
            "§5✿ §d§lEla quica, ela quica §5✿",
            "§d✿ §5§lEla quica, ela quica §d✿",
            "§5✿ §d§lEla quica, ela quica §5✿",
            "§d✿ §5§lEla quica, ela quica §d✿"
    ));

    public PossoAteNaoTeDarFloresTitle() {
        super("Posso Até Não Te Dar Flores", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§5✿ §d§lPosso até não te dar flores §5✿",
                "",
                "§7Trecho animado §5✿ §d✿ §7/ letra §d§5",
                "",
                "§8Raridade §5Mítica"
        ));
    }

    @Override
    public CollectibleMusicArtist getMusicArtist() {
        return CollectibleMusicArtist.MENO_K;
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
