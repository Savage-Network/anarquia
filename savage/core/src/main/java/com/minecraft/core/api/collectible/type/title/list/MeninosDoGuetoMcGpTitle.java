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

public class MeninosDoGuetoMcGpTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /**
     * Bordos §3✧ / §b✧; letra §b§l / §3§l — madruga em São Paulo (contraste ao Meno K).
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§3§l✧ §b§lMeninos do Gueto §7– §fMC GP §3§l✧",
            "§b§l✧ §3§lLei do Retorno acontece §b§l✧",
            "§3§l✧ §b§lCom grana, tudo é fácil §3§l✧",
            "§b§l✧ §3§lAtivo o modo esquece §b§l✧",
            "§3§l✧ §b§lNa madrugada em São Paulo §3§l✧",
            "§b§l✧ §3§lTipo um cão farejador §b§l✧",
            "§3§l✧ §b§lEu sei onde tem cheirosa §3§l✧",
            "§b§l✧ §3§lOnde cola advogada, biomédica e famosa §b§l✧",
            "§3§l✧ §b§lTodas trabalham ali §3§l✧",
            "§b§l✧ §3§lMas não nasceram aqui §b§l✧",
            "§3§l✧ §b§lParaná, Porto e Floripa, paraíso de atriz §3§l✧",
            "§b§l✧ §3§lCapital gira as nota e ela quer cirurgia §b§l✧",
            "§3§l✧ §b§lVai na gringa, busca as nota, depois fica mais bonita §3§l✧",
            "§b§l✧ §3§lProcedimento da gata é vaidade e beleza §b§l✧",
            "§3§l✧ §b§lViagem, academia, quer corpinho de princesa §3§l✧",
            "§b§l✧ §3§lProcedimento da gata é vaidade e beleza §b§l✧",
            "§3§l✧ §b§lViagem, academia, quer corpinho de princesa §7(tão vem) §3§l✧",
            "§b§l✧ §3§lMeninos do Gueto §7(MC GP) §b§l✧"
    ));

    public MeninosDoGuetoMcGpTitle() {
        super("Meninos do Gueto - MC GP", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§3§l✧ §b§lMeninos do Gueto §7– §fMC GP §3§l✧",
                "",
                "§7Trecho animado §3§b §7/ §fMC GP",
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
