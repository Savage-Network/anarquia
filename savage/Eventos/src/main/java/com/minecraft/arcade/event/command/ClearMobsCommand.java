package com.minecraft.arcade.event.command;

import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.CommandSender;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.util.Util;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class ClearMobsCommand implements CommandInheritor {

    @Command(
            name = "clearmobs",
            aliases = {"clmobs"},
            rank = RankType.MOD,
            onlyPlayer = false
    )
    public void clearMobs(BukkitCommandContext context) {

        CommandSender sender = context.getSender();

        Game game = Event.getGame();

        if (game == null || game.getWorld() == null) {
            sender.send("§cO evento não está ativo ou o mundo não foi carregado.");
            return;
        }

        List<Entity> mobs = game.getWorld().getEntities().stream()
                .filter(entity -> !(entity instanceof Player) && entity.isValid())
                .toList();

        if (mobs.isEmpty()) {
            sender.send("§cNão há mobs para remover.");
            return;
        }

        int total = 0;
        for (Entity mob : mobs) {
            mob.remove();

            total++;
        }

        sender.send("§a%s mob%s removidos do evento.".formatted(
                Util.formatNumber(total),
                total > 1 ? "s" : ""
        ));
    }
}
