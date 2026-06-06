package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.poll.SorteioService;
import com.minecraft.game.message.LanguageService;
import com.minecraft.game.message.Messages;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SorteioCommand implements CommandInheritor {

    @Command(name = "sorteio", aliases = {"raffle", "sortear"},
            permission = "game.sorteio",
            description = "Sorteia um jogador online aleatorio",
            usage = "/sorteio [quantidade]")
    public void execute(BukkitCommandContext ctx) {
        int count = 1;
        if (ctx.getArgs().length > 0) {
            try {
                count = Math.max(1, Math.min(10, Integer.parseInt(ctx.getArgs()[0])));
            } catch (NumberFormatException e) {
                ctx.replyKey("poll.sorteio.invalid-amount");
                return;
            }
        }

        List<Player> winners = SorteioService.drawMany(count);
        if (winners.isEmpty()) {
            ctx.replyKey("poll.sorteio.no-players");
            return;
        }

        Player host = ctx.isPlayer() ? ctx.player() : null;
        if (count == 1) {
            SorteioService.announceWinner(host, winners.get(0));
        } else {
            String names = winners.stream().map(Player::getName).collect(Collectors.joining(", "));
            for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                p.sendMessage(Messages.prefixed("poll.sorteio.winners", LanguageService.of(p), Map.of(
                        "players", names,
                        "count", String.valueOf(winners.size()),
                        "host", host != null ? host.getName() : "Staff")));
            }
            if (host != null) {
                host.sendMessage(Messages.prefixed("poll.sorteio.host-result-multi", LanguageService.of(host), Map.of(
                        "players", names,
                        "count", String.valueOf(winners.size()))));
            }
        }
    }
}
