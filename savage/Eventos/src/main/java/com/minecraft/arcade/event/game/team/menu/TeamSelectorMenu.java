package com.minecraft.arcade.event.game.team.menu;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.bukkit.ColorUtil;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import com.minecraft.arcade.event.game.team.Team;
import com.minecraft.arcade.event.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class TeamSelectorMenu extends Menu
{

    private final User user;

    public TeamSelectorMenu(Player player)
    {
        super(player, "Selecionar time", 1, 9);

        this.user = (User) User.of(player.getUniqueId());
    }

    @Override
    public void handle()
    {
        clear();

        Game game = Event.getGame();

        if (!game.isTeamSelect()) {
            addErrorButton("§cA seleção de times não foi ativada.");
        } else {

            List<Team> teams = game.getTeams().stream().filter(Team::isUnlocked).toList();

            if (teams.isEmpty())
                addErrorButton("§cNenhum time foi desbloqueado até o momento.");
            else
                buildPageItems(teams, 0, (team, slot) ->
                {
                    final boolean inside = user != null && user.inTeam(team);

                    addItem(slot, Item.of(Material.WOOL, ColorUtil.getIdByColor(team.getColor()), team.getColor() + "Time " + team.getName(),
                                    "§fJogadores: §7" + Util.formatNumber(team.getMembers().size()),
                                    "",
                                    inside ? "§cTime atual." : "§aClique para entrar.")
                            .click(event ->
                            {
                                if (inside) {
                                    sound(MenuSound.ERROR);
                                    return;
                                }

                                close();
                                sound(MenuSound.SUCCESS);

                                team.getMembers().add(getPlayer().getUniqueId());

                                getPlayer().sendMessage("§eVocê entrou no time " + team.getColoredName() + "§e!");
                            }));
                });
        }

        display();
    }
}
