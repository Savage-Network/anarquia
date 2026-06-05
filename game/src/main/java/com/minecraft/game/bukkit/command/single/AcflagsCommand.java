package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.menu.anticheat.AnticheatFlagsMenu;
import com.minecraft.game.util.TabCompleteUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public final class AcflagsCommand implements CommandInheritor {

    @Command(name = "acflags", rank = RankType.OWNER, playerOnly = true,
            description = "Flags/VL de um jogador (Owner)",
            usage = "/acflags <jogador>")
    public void execute(BukkitCommandContext ctx) {
        if (ctx.getArgs().length < 1) {
            ctx.reply("§cUso: /acflags <jogador>");
            return;
        }

        Player target = resolve(ctx.getArgs()[0]);
        if (target == null) {
            ctx.reply("§cJogador não encontrado ou offline.");
            return;
        }

        new AnticheatFlagsMenu(ctx.player(), target, null, 0).open();
    }

    @Completer(name = "acflags")
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) return TabCompleteUtil.onlinePlayers(ctx.getArgs()[0]);
        return List.of();
    }

    private static Player resolve(String name) {
        Player exact = Bukkit.getPlayerExact(name);
        if (exact != null) return exact;

        String lower = name.toLowerCase(Locale.ROOT);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(name)
                    || p.getName().toLowerCase(Locale.ROOT).startsWith(lower)) {
                return p;
            }
        }
        return null;
    }
}
