package com.minecraft.game.bukkit.vanish;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Eventos de proteção e ciclo de vida do vanish. */
public final class VanishListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoinHide(PlayerJoinEvent event) {
        VanishService.applyForJoining(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinAutoVanish(PlayerJoinEvent event) {
        Core.getJavaPlugin().getServer().getScheduler().runTaskLater(Core.getJavaPlugin(), () -> {
            Player player = event.getPlayer();
            if (!player.isOnline()) return;
            Account account = Core.getAccountController().of(player.getUniqueId());
            VanishService.applyAutoOnJoin(player, account);
        }, 3L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (VanishService.isVanished(event.getPlayer()) && VanishConfig.isSilentQuit()) {
            event.quitMessage(null);
        }
        VanishService.onQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!VanishConfig.isBlockPickup()) return;
        if (event.getEntity() instanceof Player p && VanishService.isVanished(p)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobTarget(EntityTargetLivingEntityEvent event) {
        if (!VanishConfig.isBlockMobTarget()) return;
        if (event.getTarget() instanceof Player p && VanishService.isVanished(p)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!VanishConfig.isBlockDamage()) return;
        if (event.getEntity() instanceof Player p && VanishService.isVanished(p)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        if (!VanishConfig.isBlockHunger()) return;
        if (event.getEntity() instanceof Player p && VanishService.isVanished(p)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPressurePlate(PlayerInteractEvent event) {
        if (!VanishConfig.isBlockPressurePlates()) return;
        if (!VanishService.isVanished(event.getPlayer())) return;
        Action action = event.getAction();
        if (action == Action.PHYSICAL) {
            event.setCancelled(true);
        }
    }
}
