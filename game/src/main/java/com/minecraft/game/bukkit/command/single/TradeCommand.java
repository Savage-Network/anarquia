package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.shop.trade.TradeManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class TradeCommand implements CommandInheritor {

    @Command(name = "trade", aliases = {"troca", "trocar"}, playerOnly = true,
            description = "Troca segura com outro jogador", usage = "/trade <jogador|aceitar|recusar>")
    public void execute(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            ctx.reply("§cUso: /trade <jogador> | /trade aceitar | /trade recusar");
            return;
        }

        Player player = ctx.player();
        String arg = ctx.getArgs()[0];

        if (arg.equalsIgnoreCase("aceitar") || arg.equalsIgnoreCase("accept")) {
            if (TradeManager.accept(player)) {
                ctx.reply("§aTroca iniciada.");
            } else {
                ctx.reply("§cNenhum convite pendente.");
            }
            return;
        }

        if (arg.equalsIgnoreCase("recusar") || arg.equalsIgnoreCase("deny")) {
            TradeManager.deny(player);
            ctx.reply("§eConvite recusado.");
            return;
        }

        Player target = Bukkit.getPlayerExact(arg);
        if (target == null) {
            ctx.reply("§cJogador offline.");
            return;
        }
        if (target.equals(player)) {
            ctx.reply("§cJogador invalido.");
            return;
        }

        TradeManager.invite(player, target);
    }

    @Completer(name = "trade", subCommands = {"troca", "trocar"})
    public List<String> tab(BukkitCommandContext ctx) {
        List<String> out = new ArrayList<>();
        String partial = ctx.getArgs().length > 0 ? ctx.getArgs()[0].toLowerCase() : "";
        if ("aceitar".startsWith(partial)) out.add("aceitar");
        if ("recusar".startsWith(partial)) out.add("recusar");
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(ctx.player()) && p.getName().toLowerCase().startsWith(partial)) {
                out.add(p.getName());
            }
        }
        return out;
    }
}
