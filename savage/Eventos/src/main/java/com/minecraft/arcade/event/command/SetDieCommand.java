package com.minecraft.arcade.event.command;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.util.Util;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetDieCommand implements CommandInheritor
{

    @Command(
            name = "setdie",
            aliases = {"sd"},
            rank = RankType.MODPLUS
    )
    public void setDie(BukkitCommandContext context)
    {

        Player player = context.getPlayer();

        String[] args = context.getArgs();

        Game game = Event.getGame();

        if (game == null) {
            player.sendMessage("§cNão foi possível carregar os dados do jogo.");
            return;
        }

        Location location = player.getLocation();

        game.setDieSpawnPoint(location);

        player.sendMessage("§aLocalização de morte definida com sucesso! (X: %s, Y: %s, Z: %s)"
                .formatted(
                        Util.formatDouble(location.getBlockX()),
                        Util.formatDouble(location.getBlockY()),
                        Util.formatDouble(location.getBlockZ())
                ));
    }
}
