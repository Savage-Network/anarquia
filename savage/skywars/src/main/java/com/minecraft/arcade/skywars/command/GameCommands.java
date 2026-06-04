package com.minecraft.arcade.skywars.command;

import com.minecraft.arcade.skywars.menu.game.teleport.TeleportMenu;
import com.minecraft.arcade.skywars.user.User;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class GameCommands implements CommandInheritor {

    @Command(name = "bussola", aliases = {"compass"})
    public void compass(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE) || !user.getArena().isPhase(RoomPhase.PLAYING)) {
            player.sendMessage("§cEste comando está indisponível no momento.");
            return;
        }

        new TeleportMenu(player).handle();
    }
}

