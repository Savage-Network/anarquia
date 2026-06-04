package com.minecraft.core.bukkit.listener;

import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.killeffect.KillEffectCollectible;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillEffectListener implements Listener {

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player))
            return;

        Player killer = event.getEntity().getKiller();
        CollectibleOperator operator = CollectibleOperator.of(killer, com.minecraft.core.api.collectible.CollectibleCategory.KILL_EFFECT);
        if (operator == null || !(operator.getCollectible() instanceof KillEffectCollectible))
            return;

        KillEffectCollectible fx = (KillEffectCollectible) operator.getCollectible();
        for (int i = 0; i < fx.getAmount(); i++) {
            double ox = (Math.random() - 0.5) * 1.2;
            double oy = Math.random() * 1.5;
            double oz = (Math.random() - 0.5) * 1.2;
            killer.getWorld().playEffect(
                    event.getEntity().getLocation().add(ox, oy, oz),
                    fx.getEffect(),
                    0);
        }
    }
}
