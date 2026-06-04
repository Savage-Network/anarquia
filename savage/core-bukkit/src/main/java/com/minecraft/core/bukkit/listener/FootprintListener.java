package com.minecraft.core.bukkit.listener;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.type.footprint.FootprintCollectible;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class FootprintListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null)
            return;
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())
            return;

        Player player = event.getPlayer();
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null)
            return;

        for (String id : account.getActiveCollectibles()) {
            if (!id.startsWith("footprint:"))
                continue;
            Collectible collectible = Core.getCollectibleController().of(id);
            if (collectible instanceof FootprintCollectible)
                ((FootprintCollectible) collectible).apply(player, to);
        }
    }
}
