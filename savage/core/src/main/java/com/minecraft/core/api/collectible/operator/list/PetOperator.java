package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.nms.module.balloon.BalloonEntity;
import com.minecraft.core.api.collectible.nms.v1_8_R3.NMS;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.pet.PetCollectible;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class PetOperator extends CollectibleOperator {

    private BalloonEntity bat;
    private BalloonEntity stand;

    public PetOperator(Player host, PetCollectible pet) {
        super(host, CollectibleCategory.PET);
        setCategoriesWithConflict(Arrays.asList(
                CollectibleCategory.BALLOON,
                CollectibleCategory.WINGS,
                CollectibleCategory.COMPANION));
        handle(pet);
    }

    @Override
    public void handle(Collectible collectible) {
        PetCollectible pet = (PetCollectible) collectible;
        bat = NMS.getInstance().createBalloonBat(getHost());
        stand = NMS.getInstance().createBalloonArmorStand(getHost(), bat, pet.getFrames());
        setCollectible(pet);
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
