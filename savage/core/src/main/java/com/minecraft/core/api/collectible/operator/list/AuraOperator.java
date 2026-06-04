package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.aura.AuraCollectible;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class AuraOperator extends CollectibleOperator {

    private BukkitTask task;

    public AuraOperator(Player host, AuraCollectible aura) {
        super(host, CollectibleCategory.AURA);
        handle(aura);
    }

    @Override
    public void handle(Collectible collectible) {
        AuraCollectible aura = (AuraCollectible) collectible;
        if (task != null) {
            task.cancel();
            task = null;
        }
        task = Bukkit.getScheduler().runTaskTimer(Core.getJavaPlugin(), () -> {
            if (getHost() == null || !getHost().isOnline()) {
                cancel();
                return;
            }
            aura.tick(getHost());
        }, 0L, 4L);
        setCollectible(aura);
    }

    @Override
    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
