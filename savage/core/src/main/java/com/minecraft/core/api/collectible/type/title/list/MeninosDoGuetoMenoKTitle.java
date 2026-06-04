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

public class MeninosDoGuetoMenoKTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Bordos §e◆ / §6◆; letra alternando §6§l / §e§l (noite tropical / baile).
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§e§l◆ §6§lMeninos do Gueto §7– §fMeno K §e§l◆",
            "§6§l◆ §e§lOlha como os preto tá reinando §6§l◆",
            "§e§l◆ §6§lMano, eu tô falando §e§l◆",
            "§6§l◆ §e§lO tanto de popô que tá piando §6§l◆",
            "§e§l◆ §6§lPra puta que desce, eu pago tantos §e§l◆",
            "§6§l◆ §e§lMandei cê deitar e toma jack, sai andando §6§l◆",
            "§e§l◆ §6§lClima tá tropical, fácil de fazer loucura §e§l◆",
            "§6§l◆ §e§lTomar uma dose do mal, curtir um baile de rua §6§l◆",
            "§e§l◆ §6§lHoje na salada é sal, só aventura noturna §e§l◆",
            "§6§l◆ §e§lJá que cê não deu moral, passa mal, filha da puta §6§l◆",
            "§e§l◆ §6§lFesta do Japa NK, elas tudo com o lança §e§l◆",
            "§6§l◆ §e§lJoga no twin, fica feliz na onda, pra sentar pra mim §6§l◆",
            "§e§l◆ §6§lMeninos do Gueto §7(Meno K) §e§l◆"
    ));

    public MeninosDoGuetoMenoKTitle() {
        super("Meninos do Gueto - Meno K", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§e§l◆ §6§lMeninos do Gueto §7– §fMeno K §e§l◆",
                "",
                "§7Trecho animado §e§6 §7/ §fMeno K",
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
