package com.minecraft.core.api.collectible.type.artifact.listener;

import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.artifact.ArtifactCollectible;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ArtifactListener implements Listener {

    @EventHandler
    public void use(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ItemStack hand = event.getItem();

        if (hand == null || hand.getType().equals(Material.AIR) || !event.getAction().name().contains("RIGHT")) return;

        CollectibleOperator operator = CollectibleOperator.of(player, CollectibleCategory.ARTIFACT);

        if (operator != null) {
            ArtifactCollectible artifact = (ArtifactCollectible) operator.getCollectible();

            if (artifact.getIcon() != null && artifact.getIcon().getType().equals(hand.getType())) {
                artifact.handle(player);

                event.setCancelled(true);
            }
        }
    }
}
