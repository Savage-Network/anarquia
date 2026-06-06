package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.poll.VoteMenu;
import com.minecraft.game.bukkit.poll.VoteService;
import com.minecraft.game.bukkit.poll.VoteSession;

public final class VotarCommand implements CommandInheritor {

    @Command(name = "votar", aliases = {"voteopen", "abrirvotacao"},
            playerOnly = true,
            description = "Abre a enquete ativa",
            usage = "/votar")
    public void execute(BukkitCommandContext ctx) {
        VoteSession session = VoteService.active();
        if (session == null || session.isExpired()) {
            ctx.replyKey("poll.vote.reopen-none");
            return;
        }
        new VoteMenu(ctx.player(), session).open();
    }
}
