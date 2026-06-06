package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;
import com.minecraft.eventos.message.EventMessages;

import java.util.Map;

public final class SumoCommand implements CommandInheritor {

    @Command(name = "sumo", permission = "eventos.admin",
            description = "Ativa o modo Sumo")
    public void execute(BukkitCommandContext ctx) {
        var engine = EventosPlugin.getInstance().getEventEngine();
        if (!engine.setMode("sumo")) {
            ctx.reply(EventMessages.format("event.error.not-waiting"));
            return;
        }
        ctx.reply(EventMessages.format("event.mode.set", Map.of("mode", "Sumo")));
    }
}
