package com.minecraft.arcade.event.user;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.user.UserModel;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import com.minecraft.arcade.event.game.state.GameState;
import com.minecraft.arcade.event.game.team.Team;
import com.minecraft.arcade.event.game.variable.Variable;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Getter
@Setter
public class User extends UserModel
{

    private int kills = 0;

    public User(UUID id, Join join)
    {
        super(id, join);
    }

    @Override
    public void handle()
    {
        Player player = getAccount().player();

        if (player != null) {
            setSidebar(new Sidebar(player, Variable.GAME_NAME.getValueAs(String.class)));

            Game game = Event.getGame();

            if (isPlayer())
                game.send(getAccount().getColoredName() + " §eentrou! (§b" + game.getOnlineCount() + "§e/§b" + game.getMaxPlayers() + "§e)");

            // Atualiza tag no tab/scoreboard com a cor do rank
            TagManager.updateTag(getAccount());

            game.handleSidebar(this);
            game.handleHotbar(player);
        }
    }

    @Override
    public void setState(ArcadeState state)
    {
        super.setState(state);

        Player player = getAccount().player();
        if (player == null) return;

        switch (state) {
            case ALIVE -> player.setFlying(false);
            case DEAD, RESPAWN -> {
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setHealth(player.getMaxHealth());
            }
        }

        Game game = Event.getGame();

        if (game != null && game.isState(GameState.PLAYING)) {

            game.handleSidebar(this);

            if (!game.isDie() && game.getDieSpawnPoint() != null) {
                player.teleport(game.getDieSpawnPoint());
            }
        }
    }

    @Override
    public boolean isSpectator()
    {
        return inState(ArcadeState.DEAD) || inState(ArcadeState.RESPAWN);
    }

    public void loadTeam()
    {
        Team team = getEventTeam();

        if (team != null) {
            Player player = getAccount().player();
            if (player == null) return;

            BukkitUtil.sendLeatherArmor(player, team.getRgb());
        }
    }

    public Team getEventTeam()
    {
        Game game = Event.getGame();

        if (!game.isTeam()) return null;

        return game.getTeams().stream()
                .filter(team -> team.getMembers().contains(getAccount().getId()))
                .findFirst()
                .orElse(null);
    }

    public boolean inTeam(Team team)
    {
        Team your = getEventTeam();

        return your != null && your.getName().equalsIgnoreCase(team.getName());
    }
}
