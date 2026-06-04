package com.minecraft.arcade.event.command;

import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import com.minecraft.arcade.event.game.state.GameState;
import com.minecraft.arcade.event.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WinnerCommand implements CommandInheritor {

    @Command(
            name = "winner",
            aliases = {"vencedor"},
            rank = RankType.ADMIN,
            onlyPlayer = false
    )
    public void winner(BukkitCommandContext context) {

        CommandSender sender = context.getSender();

        Game game = Event.getGame();

        if (!game.isState(GameState.PLAYING)) {
            sender.send("§cO evento ainda não iniciou.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUso: /" + context.getLabel() + " <jogador>.");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            sender.send("§cJogador não encontrado.");
            return;
        }
        User target = (User) User.of(targetPlayer.getUniqueId());

        if (target == null) {
            sender.send("§cJogador não encontrado.");
            return;
        }

        if (game.hasWinner()) {
            sender.send("§cO evento já possui um vencedor!");
            return;
        }

        game.setWinner(target.getAccount().getId());

        game.setState(GameState.ENDING);

        game.getAllUsers().forEach(game::handleSidebar);

        sender.send("§aO vencedor foi definido com sucesso.");
    }
}
