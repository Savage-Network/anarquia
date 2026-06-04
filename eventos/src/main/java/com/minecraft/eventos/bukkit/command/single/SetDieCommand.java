package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;
import com.minecraft.eventos.game.variable.Variable;

public final class SetDieCommand implements CommandInheritor {

    @Command(name = "setdie", permission = "eventos.admin", playerOnly = true,
            description = "Define Y de eliminacao (sumo)")
    public void execute(BukkitCommandContext ctx) {
        int y = ctx.player().getLocation().getBlockY();
        Variable.set(Variable.Key.DIE_Y, y);
        EventosPlugin.getInstance().getGame().setDieSpawnPoint(ctx.player().getLocation());
        ctx.reply("§aY de eliminacao definido para §e" + y + "§a.");
    }
}
