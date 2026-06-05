package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.annotation.Completer;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;
import com.minecraft.eventos.game.Game;
import com.minecraft.eventos.util.TabCompleteUtil;

import java.util.List;

public final class StartCommand implements CommandInheritor {

    @Command(name = "start", permission = "eventos.admin",
            description = "Inicia o countdown do evento",
            usage = "/start [segundos]")
    public void execute(BukkitCommandContext ctx) {
        Game game = EventosPlugin.getInstance().getGame();
        if (!game.canStart()) {
            ctx.reply("§cNao e possivel iniciar agora (state=" + game.getState()
                    + ", players=" + game.alives().size() + "/" + game.minPlayers() + ").");
            return;
        }
        int seconds = ctx.getArgs().length > 0
                ? safeParse(ctx.getArgs()[0], game.startCountdown())
                : game.startCountdown();
        game.requestStart(seconds);
        ctx.reply("§aEvento iniciando em §e" + seconds + "s§a.");
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
