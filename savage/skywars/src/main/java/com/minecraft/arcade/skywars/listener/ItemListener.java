package com.minecraft.arcade.skywars.listener;

import com.minecraft.arcade.skywars.arcade.arena.Arena;
import com.minecraft.arcade.skywars.menu.game.teleport.TeleportMenu;
import com.minecraft.arcade.skywars.user.User;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.route.state.ArcadeState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {

    @EventHandler
    public void onUtilityItems(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();
        if (arena == null) return;

        ItemStack hand = player.getItemInHand();

        if (arena.isPhase(RoomPhase.PLAYING) && hand != null && !hand.getType().equals(Material.AIR) && event.getAction().name().contains("RIGHT")) {

            switch (hand.getType()) {
                case COMPASS: {
                    event.setCancelled(true);
                    new TeleportMenu(player).handle();
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.setDroppedExp(0);
    }

}

