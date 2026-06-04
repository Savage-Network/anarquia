package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class WinnerCommand implements CommandInheritor {

    @Command(name = "winner", permission = "eventos.admin",
            description = "Declara vencedor do evento",
            usage = "/winner <nick>")
    public void execute(BukkitCommandContext ctx) {
        if (ctx.getArgs().length < 1) { ctx.reply("§cUso: /winner <nick>"); return; }
        Player target = Bukkit.getPlayerExact(ctx.getArgs()[0]);
        if (target == null) { ctx.reply("§cJogador nao encontrado."); return; }
        EventosPlugin.getInstance().getGame().end(target.getUniqueId());
        ctx.reply("§aVencedor declarado: §e" + target.getName());
    }
}
