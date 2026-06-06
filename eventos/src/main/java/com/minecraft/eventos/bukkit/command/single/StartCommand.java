package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.annotation.Completer;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;
import com.minecraft.eventos.message.EventMessages;
import com.minecraft.eventos.util.TabCompleteUtil;

import java.util.List;
import java.util.Map;

public final class StartCommand implements CommandInheritor {

    @Command(name = "start", permission = "eventos.admin",
            description = "Inicia o countdown do evento",
            usage = "/start [segundos]")
    public void execute(BukkitCommandContext ctx) {
        var engine = EventosPlugin.getInstance().getEventEngine();
        var session = engine.getSession();
        if (!session.canStart()) {
            ctx.reply(EventMessages.format("event.error.cannot-start", Map.of(
                    "state", session.getState().name(),
                    "players", String.valueOf(session.alives().size()),
                    "min", String.valueOf(session.minPlayers()))));
            return;
        }
        int seconds = ctx.getArgs().length > 0
                ? safeParse(ctx.getArgs()[0], session.startCountdown())
                : session.startCountdown();
        engine.startEvent(ctx.player(), seconds, false, null);
        ctx.reply(EventMessages.format("event.session.starting", Map.of(
                "seconds", String.valueOf(seconds),
                "mode", session.getActiveMode() != null ? session.getActiveMode().displayName() : session.getModeId())));
    }

    @Completer(name = "start")
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.filter(ctx.getArgs()[0], "5", "10", "15", "30", "60");
        }
        return List.of();
    }

    private int safeParse(String s, int def) {
        try { return Math.max(1, Integer.parseInt(s)); } catch (Throwable t) { return def; }
    }
}
