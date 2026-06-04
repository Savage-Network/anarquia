package com.minecraft.lobbyhost.bukkit.listener;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.architect.SignedLocation;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

/**
 * Spawn do hub: local do architect + VIP sobe 4.5 blocos e pode voar (Savage LoginListener).
 */
public final class LobbySpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpawn(PlayerSpawnLocationEvent event) {
        LobbyHostPlugin plugin = LobbyHostPlugin.getInstance();
        if (plugin == null || plugin.getArchitectLoader() == null) return;

        SignedLocation spawn = plugin.getArchitectLoader().getArchitect().location("spawn");
        if (spawn == null || spawn.toBukkit().getWorld() == null) return;

        Location loc = spawn.toBukkit().clone();
        Account account = Core.getAccountController().of(event.getPlayer().getUniqueId());

        if (account != null && account.getRankType() != RankType.MEMBER) {
            loc.add(0, 4.5, 0);
            event.getPlayer().setAllowFlight(true);
            event.getPlayer().setFlying(false);
        }

        event.setSpawnLocation(loc);
    }
}
