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

public class VidaChiqueTitle extends TitleCollectible {

    private static final int SLOW_ANIMATION_STEPS_PER_FRAME = 3;

    /** §3◇ §b◇ — tema “vida chique” (Veigh). */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§3§l◇ §b§lVida chique §3§l◇",
            "§b§l◇ §3§lEla quer um pouco de tudo que eu tenho §b§l◇",
            "§3§l◇ §b§lVampira, quer sugar minha alma §3§l◇",
            "§b§l◇ §3§lSó porque eu vim da favela §b§l◇",
            "§3§l◇ §b§lDessa vida chique ainda não entendo nada §3§l◇",
            "§b§l◇ §3§lNa cama essa garota apela §b§l◇",
            "§3§l◇ §b§lTira todas as grife, joga tudo no chão §3§l◇",
            "§b§l◇ §3§lSua cabeça gira igual ciranda §b§l◇",
            "§3§l◇ §b§lVocê me perguntando: Que horas são? §3§l◇",
            "§b§l◇ §3§lMeia-noite, você vindo pra cima de mim §b§l◇",
            "§3§l◇ §b§lGemendo, me chamando de vida §3§l◇",
            "§b§l◇ §3§lMeia-noite e quinze §b§l◇",
            "§3§l◇ §b§lMais um beck bolado, mais um copo de gin §3§l◇",
            "§b§l◇ §3§lNão tem porque fingir §b§l◇",
            "§3§l◇ §b§lNum estalar de dedo desce sua calcinha §3§l◇",
            "§b§l◇ §3§lVida chique §b§l◇"
    ));

    public VidaChiqueTitle() {
        super("Vida chique", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§3§l◇ §b§lVida chique §3§l◇",
                "",
                "§7Trecho animado §3§b §7· Veigh",
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
