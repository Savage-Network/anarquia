package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.footprint.FootprintCollectible;
import org.bukkit.entity.Player;

public class FootprintOperator extends CollectibleOperator {

    public FootprintOperator(Player host, FootprintCollectible footprint) {
        super(host, CollectibleCategory.FOOTPRINT);
        handle(footprint);
    }

    @Override
    public void handle(Collectible collectible) {
        setCollectible(collectible);
    }

    @Override
    public void cancel() {
    }
}
