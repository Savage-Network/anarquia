package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.network.EventosAccessService;

import java.util.List;
import java.util.Locale;

/** Staff no game: libera ou bloqueia entrada nos eventos na rede. */
public final class EventosAccessCommand implements CommandInheritor {

    @Command(name = "eventosacesso", aliases = {"eventosunlock", "eventoslock"},
            permission = "game.admin",
            description = "Libera ou bloqueia entrada nos eventos",
            usage = "/eventosacesso unlock|lock")
    public void execute(BukkitCommandContext ctx) {
        if (!staffOk(ctx)) return;

        if (ctx.getArgs().length == 0) {
            ctx.reply("§eUso: /eventosacesso unlock|lock");
            ctx.reply("§7No lobby use §f/eventosacesso menu §7para abrir o painel.");
            return;
        }

        String sub = ctx.getArgs()[0].toLowerCase(Locale.ROOT);
        Boolean open = switch (sub) {
            case "unlock", "on", "open", "liberar" -> true;
            case "lock", "off", "close", "fechar", "bloquear" -> false;
            default -> null;
        };
        if (open == null) {
            ctx.reply("§eUso: /eventosacesso unlock|lock");
            return;
        }
        if (!EventosAccessService.publishToggle(open)) {
            ctx.reply("§cRedis indisponível. Use §f/proxy eventos " + (open ? "unlock" : "lock") + "§c no Velocity.");
            return;
        }
        ctx.reply(open
                ? "§aEntrada nos eventos liberada na rede."
                : "§cEntrada nos eventos bloqueada na rede.");
    }

    private boolean staffOk(BukkitCommandContext ctx) {
        if (ctx.getSender().hasPermission("game.admin") || ctx.getSender().hasPermission("lobbyhost.admin")) {
            return true;
        }
        Account account = ctx.account().orElse(null);
        if (account != null && account.getRankType().isAtLeast(RankType.ADMIN)) {
            return true;
        }
        ctx.reply("§cSem permissão.");
        return false;
    }

    @Completer(name = "eventosacesso", subCommands = {"eventosunlock", "eventoslock"})
    public List<String> complete(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return List.of("unlock", "lock");
        }
        return List.of();
    }
}
