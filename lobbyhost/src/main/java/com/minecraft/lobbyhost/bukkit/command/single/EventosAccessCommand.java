package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.annotation.Completer;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.menu.eventos.EventosAccessMenu;
import com.minecraft.lobbyhost.network.EventosAccessGuard;
import com.minecraft.lobbyhost.network.EventosAccessService;

import java.util.List;
import java.util.Locale;

/**
 * Staff: libera ou bloqueia a entrada no servidor de eventos ({@code /eventosacesso menu}).
 */
public final class EventosAccessCommand implements CommandInheritor {

    @Command(name = "eventosacesso", aliases = {"eventosunlock", "eventoslock"},
            permission = "lobbyhost.admin",
            description = "Libera ou bloqueia entrada nos eventos",
            usage = "/eventosacesso [menu|unlock|lock|toggle|status]")
    public void execute(BukkitCommandContext ctx) {
        if (!staffOk(ctx)) return;

        String sub = ctx.getArgs().length > 0 ? ctx.getArgs()[0].toLowerCase(Locale.ROOT) : "menu";
        switch (sub) {
            case "menu", "gui", "abrir" -> new EventosAccessMenu(ctx.player()).open();
            case "unlock", "on", "open", "liberar" -> apply(ctx, true);
            case "lock", "off", "close", "fechar", "bloquear" -> apply(ctx, false);
            case "toggle", "alternar" -> apply(ctx, !EventosAccessGuard.isOpen());
            case "status", "info" -> ctx.reply("§6Eventos: " + EventosAccessService.statusColored());
            default -> ctx.reply("§eUso: /eventosacesso [menu|unlock|lock|toggle|status]");
        }
    }

    private void apply(BukkitCommandContext ctx, boolean open) {
        if (EventosAccessGuard.isOpen() == open) {
            ctx.reply("§7Eventos já está " + (open ? "§aaberto" : "§cfechado") + "§7.");
            return;
        }
        if (!EventosAccessService.publishToggle(open)) {
            ctx.reply("§cRedis indisponível. Use §f/proxy eventos " + (open ? "unlock" : "lock") + "§c no Velocity.");
            return;
        }
        EventosAccessGuard.syncFromNetwork(open);
        ctx.reply(open
                ? "§aEntrada nos eventos liberada na rede."
                : "§cEntrada nos eventos bloqueada na rede.");
    }

    private boolean staffOk(BukkitCommandContext ctx) {
        if (ctx.getSender().hasPermission("lobbyhost.admin") || ctx.getSender().hasPermission("game.admin")) {
            return true;
        }
        Account account = ctx.account().orElse(null);
        if (account != null && account.getRankType().isAtLeast(
                com.minecraft.lobbyhost.account.context.objects.rank.RankType.ADMIN)) {
            return true;
        }
        ctx.reply("§cSem permissão.");
        return false;
    }

    @Completer(name = "eventosacesso", subCommands = {"eventosunlock", "eventoslock"})
    public List<String> complete(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return filter(List.of("menu", "unlock", "lock", "toggle", "status"), ctx.getArgs()[0]);
        }
        return List.of();
    }

    private static List<String> filter(List<String> options, String partial) {
        String needle = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(o -> needle.isEmpty() || o.startsWith(needle))
                .toList();
    }
}
