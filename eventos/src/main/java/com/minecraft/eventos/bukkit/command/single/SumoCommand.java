package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;
import com.minecraft.eventos.game.Game;

public final class SumoCommand implements CommandInheritor {

    @Command(name = "sumo", permission = "eventos.admin",
            description = "Alterna modo Sumo")
    public void execute(BukkitCommandContext ctx) {
        Game game = EventosPlugin.getInstance().getGame();
        game.setSumo(!game.isSumo());
        ctx.reply(game.isSumo()
                ? "§aModo Sumo ATIVADO. Eliminacao por queda habilitada."
                : "§eModo Sumo DESATIVADO.");
    }
}
