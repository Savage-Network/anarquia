package com.minecraft.arcade.thebridge.listener;

import com.minecraft.arcade.thebridge.arcade.Arcade;
import com.minecraft.arcade.thebridge.arcade.arena.Arena;
import com.minecraft.arcade.thebridge.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.core.bukkit.manager.Manager;
import com.minecraft.core.arcade.ArcadeHolder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

public class ArcadeListener implements Listener {

    private final Manager manager = BukkitCore.getManager();

    @EventHandler
    public void onSyncUpdate(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (ArcadeHolder holder : manager.getArcade().getArcades()) {
                if (!(holder instanceof Arcade arcade)) continue;

                List<Arena> arenaList = arcade.getRooms().stream().map(room -> (Arena) room).toList();

                arenaList.forEach(arena -> {
                    arena.handlePulse();

                    if (arena.isPhase(RoomPhase.PLAYING)) {
                        for (Player player : arena.getPlayers()) {
                            User user = (User) User.of(player.getUniqueId());

                            if (user != null && user.getSidebar() != null) {
                                if (arcade instanceof com.minecraft.arcade.thebridge.arcade.list.Solo solo) {
                                    solo.updateTeamStyle(arena, user.getSidebar());
                                }
                            }
                        }
                    }

                    arcade.checkAndGenerateArenas();
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer()) return;

        if (user.isProtected())
            event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer()) return;

        Arena arena = user.getArena();

        if (user.inState(com.minecraft.core.arcade.route.state.ArcadeState.RESPAWN)) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                    event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                    event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                event.setCancelled(true);
            }
        }

        Location to = event.getTo(), spawn = arena.getLocation("spawn");

        if (to.getY() <= (spawn.getY() - 20)) {
            if (!user.isProtected()) {
                User killer = user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null;

                arena.getArcade().handleDeath(user, killer, Arcade.DeathCause.VOID);
            } else
                player.teleport(spawn);
        }
    }
}
