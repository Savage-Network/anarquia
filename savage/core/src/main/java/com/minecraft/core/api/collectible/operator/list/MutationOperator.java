package com.minecraft.core.api.collectible.operator.list;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.mutation.MutationCollectible;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class MutationOperator extends CollectibleOperator {

    private BukkitTask task;

    public MutationOperator(Player host, MutationCollectible mutation) {
        super(host, CollectibleCategory.MUTATION);
        handle(mutation);
    }

    @Override
    public void handle(Collectible collectible) {
        MutationCollectible mutation = (MutationCollectible) collectible;
        if (task != null) {
            task.cancel();
            task = null;
        }
        task = Bukkit.getScheduler().runTaskTimer(Core.getJavaPlugin(), () -> {
            Player player = getHost();
            if (player == null || !player.isOnline()) {
                cancel();
                return;
            }
            Location loc = player.getLocation().add(0, 1, 0);
            Effect effect = mutation.getEffect();
            int count = mutation.getParticleAmount() + (mutation.isGlow() ? 4 : 0);
            for (int i = 0; i < count; i++) {
                double ox = (Math.random() - 0.5) * 0.8;
                double oz = (Math.random() - 0.5) * 0.8;
                player.getWorld().playEffect(loc.clone().add(ox, Math.random() * 0.5, oz), effect, 0);
            }
        }, 0L, 6L);
        setCollectible(mutation);
    }

    @Override
    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
