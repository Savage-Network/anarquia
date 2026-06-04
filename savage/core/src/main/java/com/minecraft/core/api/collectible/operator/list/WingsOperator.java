package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.nms.module.balloon.BalloonEntity;
import com.minecraft.core.api.collectible.nms.v1_8_R3.NMS;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.wings.WingsCollectible;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class WingsOperator extends CollectibleOperator {

    private BalloonEntity bat;
    private BalloonEntity stand;

    public WingsOperator(Player host, WingsCollectible wings) {
        super(host, CollectibleCategory.WINGS);
        setCategoriesWithConflict(Arrays.asList(
                CollectibleCategory.BALLOON,
                CollectibleCategory.PET,
                CollectibleCategory.COMPANION));
        handle(wings);
    }

    @Override
    public void handle(Collectible collectible) {
        WingsCollectible wings = (WingsCollectible) collectible;
        bat = NMS.getInstance().createBalloonBat(getHost());
        stand = NMS.getInstance().createBalloonArmorStand(getHost(), bat, wings.getFrames());
        setCollectible(wings);
    }

    @Override
    public void cancel() {
        if (bat != null) {
            bat.kill();
            bat = null;
        }
        if (stand != null) {
            stand.kill();
            stand = null;
        }
    }
}
