package com.minecraft.core.bukkit.listener;

import com.minecraft.core.Core;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.type.jumpeffect.JumpEffectCollectible;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JumpEffectListener implements Listener {

    private final Map<UUID, Boolean> onGround = new ConcurrentHashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();

        boolean grounded = player.isOnGround();
        Boolean was = onGround.put(id, grounded);

        if (was != null && was && !grounded && event.getTo() != null
                && event.getTo().getY() > event.getFrom().getY() + 0.08) {

            CollectibleOperator operator = CollectibleOperator.of(player, CollectibleCategory.JUMP_EFFECT);
            if (operator != null && operator.getCollectible() instanceof JumpEffectCollectible) {
                ((JumpEffectCollectible) operator.getCollectible()).burst(player);
            }
        }
    }
}
