package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.deatheffect.DeathEffectCollectible;
import org.bukkit.entity.Player;

public class DeathEffectOperator extends CollectibleOperator {

    public DeathEffectOperator(Player host, DeathEffectCollectible effect) {
        super(host, CollectibleCategory.DEATH_EFFECT);
        handle(effect);
    }

    @Override
    public void handle(Collectible collectible) {
        setCollectible(collectible);
    }

    @Override
    public void cancel() {
    }
}
