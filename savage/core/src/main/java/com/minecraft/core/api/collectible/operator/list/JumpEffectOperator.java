package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.jumpeffect.JumpEffectCollectible;
import org.bukkit.entity.Player;

public class JumpEffectOperator extends CollectibleOperator {

    public JumpEffectOperator(Player host, JumpEffectCollectible effect) {
        super(host, CollectibleCategory.JUMP_EFFECT);
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
