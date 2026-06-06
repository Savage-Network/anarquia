package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.poll.VoteService;
import com.minecraft.game.util.TabCompleteUtil;

import java.util.ArrayList;
import java.util.List;

public final class VotacaoCommand implements CommandInheritor {

    @Command(name = "votacao", aliases = {"votação", "vote", "enquete", "poll"},
            permission = "game.votacao",
            description = "Cria uma enquete com GUI de bau",
            usage = "/votacao <opcao1> <opcao2> [opcao3] [opcao4] <tempo_seg>")
    public void execute(BukkitCommandContext ctx) {
        if (ctx.getArgs().length < 3) {
            ctx.replyKey("poll.vote.usage");
            return;
        }

        if (VoteService.hasActive()) {
            ctx.replyKey("poll.vote.already-active");
            return;
        }

        String last = ctx.getArgs()[ctx.getArgs().length - 1];
        int duration;
        try {
            duration = Integer.parseInt(last);
        } catch (NumberFormatException e) {
            ctx.replyKey("poll.vote.invalid-time");
            return;
        }

        List<String> options = new ArrayList<>();
        for (int i = 0; i < ctx.getArgs().length - 1; i++) {
            options.add(ctx.getArgs()[i]);
        }

        if (options.size() < 2 || options.size() > 4) {
            ctx.replyKey("poll.vote.invalid-options");
            return;
        }

        if (!ctx.isPlayer()) {
            ctx.replyKey("command.player-only");
            return;
        }

        if (!VoteService.start(ctx.player(), options, duration)) {
            ctx.replyKey("poll.vote.start-failed");
            return;
        }

        ctx.replyKey("poll.vote.created", java.util.Map.of(
                "seconds", String.valueOf(duration),
                "options", String.join(", ", options)));
    }

    @Command(name = "votacaoencerrar", aliases = {"encerrarvotacao", "stopvote"},
            permission = "game.votacao",
            description = "Encerra a votacao ativa",
            usage = "/votacaoencerrar")
    public void stop(BukkitCommandContext ctx) {
        if (!VoteService.hasActive()) {
            ctx.replyKey("poll.vote.none-active");
            return;
        }
        VoteService.endNow();
        ctx.replyKey("poll.vote.stopped");
    }

    @Completer(name = "votacao")
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length <= 4) {
            return TabCompleteUtil.filter(ctx.getArgs()[ctx.getArgs().length - 1],
                    "30", "60", "120", "Sim", "Nao", "A", "B");
        }
        return List.of();
    }
}
