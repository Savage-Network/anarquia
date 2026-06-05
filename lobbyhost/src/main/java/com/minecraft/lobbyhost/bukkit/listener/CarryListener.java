package com.minecraft.lobbyhost.bukkit.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;


/**
 * Handles a simple "carry" mechanic.
 *
 * <ul>
 *   <li>Shift + Right‑click on another player → pick them up (makes them a passenger).</li>
 *   <li>Shift + Left‑click on a player you are carrying → throw them forward.</li>
 * </ul>
 */
public final class CarryListener implements Listener {

    /**
     * Pick up the target player as a passenger when the attacker is sneaking and right‑clicks.
     */
    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        Player carrier = event.getPlayer();
        if (!carrier.isSneaking()) return;
        if (!(event.getRightClicked() instanceof Player target)) return;
        // Avoid recursion / self‑carry
        if (carrier.equals(target)) return;
        // If already a passenger, do nothing
        if (carrier.getPassengers().contains(target)) return;
        // Add as passenger
        carrier.addPassenger(target);
        carrier.sendMessage("§aVocê pegou " + target.getName() + ".");
        target.sendMessage("§eVocê foi pego por " + carrier.getName() + ".");
    }

    /**
     * Throw the carried player when the carrier is sneaking and left‑clicks the carried player.
     */
    @EventHandler
    public void onLeftClickAir(PlayerInteractEvent event) {
        Player carrier = event.getPlayer();
        if (!carrier.isSneaking()) return;
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) return;
        // Find a passenger that is a Player
        for (Entity passenger : carrier.getPassengers()) {
            if (passenger instanceof Player target) {
                // Remove passenger relationship and throw
                carrier.removePassenger(target);
                Vector forward = carrier.getLocation().getDirection().multiply(1.5).setY(0.5);
                target.setVelocity(forward);
                carrier.sendMessage("§aVocê jogou " + target.getName() + ".");
                target.sendMessage("§eVocê foi jogado por " + carrier.getName() + ".");
                break;
            }
        }
    }
    // Throw via left-click on passenger (damage event)
    @EventHandler
    public void onLeftClickEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player carrier)) return;
        if (!carrier.isSneaking()) return;
        if (!(event.getEntity() instanceof Player target)) return;
        // Ensure target is passenger of carrier
        if (!carrier.getPassengers().contains(target)) return;
        event.setCancelled(true);
        carrier.removePassenger(target);
        Vector forward = carrier.getLocation().getDirection().multiply(1.5).setY(0.5);
        target.setVelocity(forward);
        carrier.sendMessage("§aVocê jogou " + target.getName() + ".");
        target.sendMessage("§eVocê foi jogado por " + carrier.getName() + ".");
    }
}
