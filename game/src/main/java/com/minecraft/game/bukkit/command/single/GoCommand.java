package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.staff.StaffGoService;
import com.minecraft.game.util.ModernText;
import com.minecraft.game.util.TabCompleteUtil;
import org.bukkit.entity.Player;

import java.util.List;

/** /go — teleporta staff até um jogador (mesmo servidor ou outro da rede). */
public final class GoCommand implements CommandInheritor {

    @Command(name = "go", playerOnly = true, permission = "game.go",
            rank = RankType.HELPER,
            description = "Teleporta até um jogador na rede",
            usage = "/go <jogador>")
    public void go(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        Player staff = ctx.player();
        if (account == null) {
            ctx.replyKey("errors.account-not-loaded");
            return;
        }
        if (!StaffGoService.canUse(account, staff)) {
            ctx.reply(ModernText.parse("<!italic><" + ModernText.ERROR + ">Sem permissão."));
            return;
        }
        if (ctx.getArgs().length < 1) {
            ctx.reply(ModernText.parse(ModernText.hint("go <jogador>")));
            return;
        }
        StaffGoService.go(staff, ctx.getArgs()[0]);
    }

    @Completer(name = "go")
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.onlinePlayers(ctx.getArgs()[0]);
        }
        return List.of();
    }
}
