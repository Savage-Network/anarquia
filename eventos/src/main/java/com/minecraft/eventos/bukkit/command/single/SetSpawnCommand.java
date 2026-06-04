package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;

public final class SetSpawnCommand implements CommandInheritor {

    @Command(name = "setspawn", permission = "eventos.admin", playerOnly = true,
            description = "Define o spawn do evento")
    public void execute(BukkitCommandContext ctx) {
        EventosPlugin.getInstance().getGame().setSpawn(ctx.player().getLocation());
        ctx.reply("§aSpawn do evento definido na sua posicao.");
    }
}
