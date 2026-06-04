package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.vanish.VanishService;

public final class VanishCommand implements CommandInheritor {

    @Command(name = "vanish", aliases = {"v"}, playerOnly = true,
            permission = "lobbyhost.vanish",
            description = "Alterna invisibilidade (staff)",
            usage = "/vanish [on|off]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply("§cConta nao carregada.");
            return;
        }
        if (!VanishService.canVanish(account)) {
            ctx.reply("§cSem permissao.");
            return;
        }
        var player = ctx.player();
        if (ctx.getArgs().length == 0) {
            VanishService.toggle(player);
            return;
        }
        switch (ctx.getArgs()[0].toLowerCase()) {
            case "on", "true", "1" -> VanishService.setVanished(player, true);
            case "off", "false", "0" -> VanishService.setVanished(player, false);
            default -> ctx.reply("§e/vanish §7| §e/vanish on|off");
        }
    }
}
