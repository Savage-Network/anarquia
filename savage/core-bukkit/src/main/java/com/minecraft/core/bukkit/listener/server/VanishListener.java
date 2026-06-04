package com.minecraft.core.bukkit.listener.server;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.bukkit.api.vanish.Vanish;
import com.minecraft.core.bukkit.event.type.account.AccountVanishLogEvent;
import com.minecraft.core.bukkit.user.UserModel;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import static com.minecraft.core.bukkit.api.vanish.Vanish.hideAndShow;

public class VanishListener implements Listener {

    @EventHandler
    public void log(AccountVanishLogEvent event) {
        Account account = event.getAccount();

        /* Em servidores arcade */
        if (Core.getServerType().isArcade()) {
            UserModel user = UserModel.of(account.getId());

            /* Alternar a entrada para VANISH */
            if (user != null)
                user.setJoin(Join.VANISH);
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Vanish.getList().forEach(vanish -> hideAndShow(vanish.getPlayer(), AccountVanishLogEvent.VanishState.JOIN));
    }

    @EventHandler
    public void leave(PlayerQuitEvent event) {
        Vanish.remove(event.getPlayer());
    }

    @EventHandler
    public void kicked(PlayerKickEvent event) {
        Vanish.remove(event.getPlayer());
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        if (Vanish.has(player))
            event.setCancelled(true);
    }

    @EventHandler
    public void vanishInteract(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer(), clicked = (Player) event.getRightClicked();

            ItemStack hand = player.getItemInHand();

            if (Vanish.has(player)) {
                /* Abrir inventário */
                if (hand == null || hand.getType().equals(Material.AIR)) {
                    player.performCommand("invsee " + clicked.getName());
                    return;
                }
            }
        }
    }
}
