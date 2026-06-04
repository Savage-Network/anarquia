package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.killeffect.KillEffectCollectible;
import org.bukkit.entity.Player;

public class KillEffectOperator extends CollectibleOperator {

    public KillEffectOperator(Player host, KillEffectCollectible effect) {
        super(host, CollectibleCategory.KILL_EFFECT);
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
