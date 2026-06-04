package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.CollectibleTitleGroup;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SavageTitle extends TitleCollectible {

    /**
     * Todas as cores de chat do Minecraft (§0–§f), em ordem oficial.
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§f§lSAVAGE",
            "§2§lS§fAVAGE",
            "§fS§2§lA§fVAGE",
            "§fS§2§lA§fVAGE",
            "§fSA§2§lV§fAGE",
            "§fSAV§2§lA§fGE",
            "§fSAVA§2§lG§fE",
            "§fSAVAG§2§lE",
            "§2§lSAVAGE"
    ));

    public SavageTitle() {
        super("Savage", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§2§lProtagonista",
                "",
                "",
                "§8Raridade §5Mítica§8"
        ));
    }

    @Override
    public CollectibleTitleGroup getTitleGroup() {
        return CollectibleTitleGroup.AURA;
    }

    @Override
    public String getTitle() {
        return ANIMATION_FRAMES.get(0);
    }

    @Override
    public List<String> getTitleAnimationFrames() {
        return ANIMATION_FRAMES;
    }
}
