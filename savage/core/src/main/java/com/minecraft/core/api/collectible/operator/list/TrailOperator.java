package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.trail.TrailCollectible;
import org.bukkit.entity.Player;

public class TrailOperator extends CollectibleOperator {

    public TrailOperator(Player host, TrailCollectible trail) {
        super(host, CollectibleCategory.TRAIL);
        handle(trail);
    }

    @Override
    public void handle(Collectible collectible) {
        TrailCollectible trail = (TrailCollectible) collectible;
        
        // Apenas ativar o rastro, o TrailListener vai cuidar de aplicar
        setCollectible(trail);
    }

    @Override
    public void cancel() {
        // Limpar rastros quando desativar
    }
}
