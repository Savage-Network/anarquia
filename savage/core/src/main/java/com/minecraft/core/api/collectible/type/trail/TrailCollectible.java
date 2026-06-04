package com.minecraft.core.api.collectible.type.trail;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class TrailCollectible extends Collectible {

    public TrailCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, long createdAt) {
        super(name, CollectibleCategory.TRAIL, rarity, ranks, createdAt);
    }

    /**
     * Aplica o efeito do rastro na localização do jogador
     */
    public abstract void applyTrail(Player player, Location location);

    /**
     * Cancela/remove o rastro
     */
    public abstract void removeTrail(Location location);
}
