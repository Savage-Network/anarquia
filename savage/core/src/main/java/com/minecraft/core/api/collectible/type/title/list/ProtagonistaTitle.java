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

public class ProtagonistaTitle extends TitleCollectible {

    /**
     * Todas as cores de chat do Minecraft (§0–§f), em ordem oficial.
     */
    private static final List<String> ANIMATION_FRAMES = Collections.unmodifiableList(Arrays.asList(
            "§0§lPROTAGONISTA",
            "§1§lPROTAGONISTA",
            "§2§lPROTAGONISTA",
            "§3§lPROTAGONISTA",
            "§4§lPROTAGONISTA",
            "§5§lPROTAGONISTA",
            "§6§lPROTAGONISTA",
            "§7§lPROTAGONISTA",
            "§8§lPROTAGONISTA",
            "§9§lPROTAGONISTA",
            "§a§lPROTAGONISTA",
            "§b§lPROTAGONISTA",
            "§c§lPROTAGONISTA",
            "§d§lPROTAGONISTA",
            "§e§lPROTAGONISTA",
            "§f§lPROTAGONISTA"
    ));

    public ProtagonistaTitle() {
        super("Protagonista", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.OWNER), System.currentTimeMillis());

        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§6§lProtagonista§r §8— §7cena é sua.",
                "",
                "§7Letreiro animado com §ftodas as 16 cores §7de chat:",
                "§0■§1■§2■§3■§4■§5■§6■§7■§8■§9■§a■§b■§c■§d■§e■§f■",
                "",
                "§8Raridade §5Mítica§8 · §7cicla §80–9§7 e §fa–f"
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
