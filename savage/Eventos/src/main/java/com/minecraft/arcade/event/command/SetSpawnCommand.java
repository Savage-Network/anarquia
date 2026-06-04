package com.minecraft.arcade.event.command;

import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandInheritor {

    @Command(
            name = "setspawn",
            rank = RankType.ADMIN
    )
    public void setSpawn(BukkitCommandContext context) {
        Player player = context.getPlayer();

        Game game = Event.getGame();

        game.setSpawn(player.getLocation());

        player.sendMessage("§aO spawn foi definido com sucesso.");
    }
}
