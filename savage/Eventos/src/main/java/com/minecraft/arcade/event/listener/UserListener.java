package com.minecraft.arcade.event.listener;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.manager.list.PermissionManager;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import com.minecraft.arcade.event.game.state.GameState;
import com.minecraft.arcade.event.game.variable.Variable;
import com.minecraft.arcade.event.user.User;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class UserListener implements Listener
{

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        Player player = event.getPlayer();

        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account == null) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
            return;
        }

        if (Boolean.FALSE.equals(Variable.JOIN.getValueAs(Boolean.class)) && !account.isStaffer()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cO evento está disponível somente para membros da equipe.");
            return;
        }

        Game game = Event.getGame();

        if (game.isFull() && !account.isVIP()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cO evento está cheio!");
            return;
        }

        Join join = game.isState(GameState.PLAYING) ? Join.SPECTATOR : Join.PLAYER;

        new User(player.getUniqueId(), join);
    }

    @EventHandler
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event)
    {
        event.setSpawnLocation(Event.getGame().getSpawn());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) {
            player.kickPlayer(Constant.ACCOUNT_LOAD_FAILED_MESSAGE);
            return;
        }

        Account account = user.getAccount();

        if (account.hasRank(RankType.ADMIN)) {
            PermissionManager.loadPermissions(player);
        }

        user.handle();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        User user = (User) User.of(event.getPlayer().getUniqueId());

        if (user != null) {
            Account account = user.getAccount();
            Game game = Event.getGame();
            if (game != null && user.isPlayer())
                game.send(account.getColoredName() + "§e saiu! (§b" + (game.getOnlineCount() - 1) + "§e/§b" + game.getMaxPlayers() + "§e)");

            User.remove(user.getAccount().getId());
        }
    }
}
