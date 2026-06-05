package com.minecraft.game.bukkit.listener;

import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * O End esta desativado no SavageMC. Staff com {@code game.admin} pode entrar via {@code /mundo}.
 */
public final class EndWorldListener implements Listener {

    private static final String BLOCK_MSG = "§cO End esta desativado no SavageMC.";

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(BLOCK_MSG);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!isEnd(event.getTo() == null ? null : event.getTo().getWorld())) return;
        if (canAccessEnd(player)) return;

        event.setCancelled(true);
        player.sendMessage(BLOCK_MSG);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        ejectIfNeeded(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        ejectIfNeeded(event.getPlayer());
    }

    private static void ejectIfNeeded(Player player) {
        if (!isEnd(player.getWorld()) || canAccessEnd(player)) return;

        player.sendMessage(BLOCK_MSG);
        player.teleport(SemiAnarchyConfig.spawnPoint());
    }

    private static boolean isEnd(World world) {
        return world != null && world.getEnvironment() == World.Environment.THE_END;
    }

    private static boolean canAccessEnd(Player player) {
        return player.hasPermission("game.admin");
    }
}
