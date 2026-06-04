package com.minecraft.core.bukkit.listener;

import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.type.deatheffect.DeathEffectCollectible;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEffectListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        CollectibleOperator operator = CollectibleOperator.of(player, CollectibleCategory.DEATH_EFFECT);
        if (operator == null || !(operator.getCollectible() instanceof DeathEffectCollectible))
            return;

        DeathEffectCollectible fx = (DeathEffectCollectible) operator.getCollectible();
        fx.play(player, player.getLocation());
    }
}
