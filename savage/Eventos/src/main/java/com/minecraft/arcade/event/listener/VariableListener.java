package com.minecraft.arcade.event.listener;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import com.minecraft.arcade.event.game.variable.Variable;
import com.minecraft.arcade.event.user.User;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class VariableListener implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Game game = Event.getGame();
        if (game == null) return;

        if (!game.isState(com.minecraft.arcade.event.game.state.GameState.PLAYING)) {
            event.setCancelled(true);
            return;
        }

        if (!game.isDie())
            user.setState(ArcadeState.ALIVE);
        else {
            if (game.isSpec())
                user.setState(ArcadeState.DEAD);
            else {
                if (user.getAccount().isStaffer()) {
                    user.setJoin(Join.VANISH);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                } else
                    user.getAccount().redirect(ServerType.HUB);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Game game = Event.getGame();
        if (game == null) return;

        if (user.isSpectator() && event.getTo().getY() < 0) {
            player.teleport(game.getSpawn());
            return;
        }

        if (event.getTo().getBlock().getType().equals(Material.STATIONARY_WATER) &&
            player.getLocation().getBlock().getType().equals(Material.AIR)) {

            if (!game.isDie())
                user.setState(ArcadeState.ALIVE);
            else {
                if (!game.isSpec())
                    if (user.getAccount().isStaffer()) {
                        user.setJoin(Join.VANISH);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    } else
                        user.getAccount().redirect(ServerType.HUB);
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Game game = Event.getGame();
        if (game == null) return;

        Account account = user.getAccount();

        String title = user.isSpectator() ? "§7(SPEC) " : "";

        event.setFormat(title + account.getColoredName() + "§8: §f" + event.getMessage());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = (User) User.of(player.getUniqueId());

        if (user != null) {
            user.setState(ArcadeState.ALIVE);
        }
    }
}
