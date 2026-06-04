package com.minecraft.arcade.skywars.command;

import com.minecraft.arcade.skywars.arcade.Arcade;
import com.minecraft.arcade.skywars.arcade.arena.Arena;
import com.minecraft.arcade.skywars.user.User;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class PlayCommands implements CommandInheritor {

    @Command(name = "start", rank = RankType.MODPLUS)
    public void start(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) {
            player.sendMessage("§cVocê não está em uma arena!");
            return;
        }

        Arena arena = user.getArena();

        if (arena.isPhase(RoomPhase.PLAYING)) {
            player.sendMessage("§cA partida já iniciou!");
            return;
        }

        arena.setPhase(RoomPhase.PLAYING);
        player.sendMessage("§aPartida iniciada!");
    }

    @Command(name = "playagain")
    public void playAgain(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) {
            player.sendMessage("§cVocê não está em uma arena!");
            return;
        }

        Arcade arcade = user.getArcade();

        if (arcade == null) {
            player.sendMessage("§cErro ao encontrar arcade!");
            return;
        }

        Arena arena = user.getArena();

        user.reset();

        new ArenaSearchMessage(player.getUniqueId(), ArcadeRouteContext.builder()
                .arcade(arcade.getCategory())
                .slot(arena.getSlot())
                .type(arena.getType())
                .join(user.getJoin())
                .build()).send();
    }
}

