package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.vanish.VanishService;

/** /go — atalho de vanish para staff. */
public final class GoCommand implements CommandInheritor {

    @Command(name = "go", playerOnly = true,
            permission = "lobbyhost.vanish",
            description = "Atalho de vanish (staff)")
    public void go(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.replyKey("errors.account-not-loaded"); return; }
        if (!VanishService.canVanish(account)) { ctx.reply("§cSem permissão."); return; }
        VanishService.toggle(ctx.player());
    }
}
