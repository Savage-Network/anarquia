package com.minecraft.eventos.bukkit.command.single;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.bukkit.command.annotation.Command;
import com.minecraft.eventos.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.eventos.bukkit.command.structure.CommandInheritor;
import com.minecraft.eventos.game.skit.SkitController;
import org.bukkit.entity.Player;

public final class SkitCommand implements CommandInheritor {

    @Command(name = "skit", permission = "eventos.admin",
            description = "Aplica/lista kits temporarios",
            usage = "/skit <list|apply> [nome] [jogador]")
    public void execute(BukkitCommandContext ctx) {
        SkitController controller = EventosPlugin.getInstance().getSkitController();
        if (ctx.getArgs().length == 0) {
            ctx.reply("§eUso: /skit <list|apply> [nome] [jogador]");
            return;
        }

        switch (ctx.getArgs()[0].toLowerCase()) {
            case "list" -> ctx.reply("§eKits disponiveis: §f" + String.join(", ", controller.available()));
            case "apply" -> {
                if (ctx.getArgs().length < 2) { ctx.reply("§cUso: /skit apply <nome> [jogador]"); return; }
                String kit = ctx.getArgs()[1];
                Player target = ctx.getArgs().length >= 3
                        ? org.bukkit.Bukkit.getPlayerExact(ctx.getArgs()[2])
                        : ctx.player();
                if (target == null) { ctx.reply("§cJogador nao encontrado."); return; }
                boolean applied = controller.apply(target, kit);
                ctx.reply(applied
                        ? "§aKit §e" + kit + " §aaplicado em §e" + target.getName() + "§a."
                        : "§cKit '" + kit + "' nao existe.");
            }
            default -> ctx.reply("§cSubcomando desconhecido.");
        }
    }
}
