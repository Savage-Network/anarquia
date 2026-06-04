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

public class AnosLuzTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Bordos §5✧ / §d✧; letra alternando §d§l / §5§l (neon “distância / foguete”).
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§5§l✧ §d§lAnos Luz §5§l✧",
            "§d§l✧ §5§lEstamos anos luz daquele lugar §d§l✧",
            "§5§l✧ §d§lNem sei como eu vim parar aqui §5§l✧",
            "§d§l✧ §5§lTua língua eu não entendo §d§l✧",
            "§5§l✧ §d§lEu só sei que tu é chata e muito gata pra mim §5§l✧",
            "§d§l✧ §5§lEu vim de lá, eu vim de longe, de longe de cá §d§l✧",
            "§5§l✧ §d§lEu vim pra te ensinar que quando o beat dropa a gente nunca mais pode parar §7(tsey) §5§l✧",
            "§d§l✧ §5§lE quando a gente cai na cama §d§l✧",
            "§5§l✧ §d§lO bagulho esquenta quente na frigideira §7(quente na frigideira) §5§l✧",
            "§d§l✧ §5§lQuando eu bato o olho nela não paro de imaginar besteira §7(eu só penso besteira) §d§l✧",
            "§5§l✧ §d§lSinceramente, o problema é ela porque essa bunda é brincadeira §7(essa bunda é brincadeira) §5§l✧",
            "§d§l✧ §5§lQuem ela pensa que é esfregando essa porra em mim a noite inteira §d§l✧",
            "§5§l✧ §d§lDropando o doce em mim a noite inteira §5§l✧",
            "§d§l✧ §5§lFumando um beckzinho a noite inteira §d§l✧",
            "§5§l✧ §d§lTô com ela em um foguete de bobeira §5§l✧",
            "§d§l✧ §5§lTa me enlouquecendo mesmo que eu não queira §7(mesmo que eu não queira) §d§l✧",
            "§5§l✧ §d§lAnos Luz §5§l✧"
    ));

    public AnosLuzTitle() {
        super("Anos Luz", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§5§l✧ §d§lAnos Luz §5§l✧",
                "",
                "§7Trecho animado §5§d",
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
