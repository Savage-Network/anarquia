package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;

public final class EventosReloadCommand implements CommandInheritor {

    @Command(name = "eventosreload", permission = "eventos.admin",
            description = "Recarrega configuracoes do eventos.jar")
    public void execute(BukkitCommandContext ctx) {
        EventosPlugin.getInstance().reloadAll();
        ctx.reply("§aConfiguracoes do eventos.jar recarregadas.");
    }
}
