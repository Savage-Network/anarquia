package com.minecraft.arcade.event.command;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import com.minecraft.arcade.event.game.state.GameState;

public class StartCommand implements CommandInheritor {

    @Command(
            name = "start",
            aliases = {"iniciar"},
            rank = RankType.MOD
    )
    public void start(BukkitCommandContext context) {
        CommandSender sender = context.getSender();

        Game game = Event.getGame();

        if (game.isState(GameState.PLAYING)) {
            sender.send("§cO evento já está em andamento.");
            return;
        }

        if (game.isState(GameState.ENDING)) {
            sender.send("§cO evento está encerrando.");
            return;
        }

        if (game.getOnlineCount() < 1) {
            sender.send("§cNão há jogadores suficientes para iniciar.");
            return;
        }

        game.setState(GameState.PLAYING);
        game.getAllUsers().forEach(game::handleSidebar);

        game.send("§aO evento foi iniciado por um administrador!");
        sender.send("§aEvento iniciado com sucesso.");
    }
}
