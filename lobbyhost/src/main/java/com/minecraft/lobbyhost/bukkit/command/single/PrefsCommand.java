package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import com.minecraft.lobbyhost.bukkit.menu.prefs.PrefsMenu;

public final class PrefsCommand implements CommandInheritor {

    @Command(name = "prefs", aliases = {"preferencias"}, playerOnly = true,
            description = "Preferencias da conta (menu)",
            usage = "/prefs")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.reply("§cConta nao carregada.");
            return;
        }
        new PrefsMenu(ctx.player(), account).open();
    }
}
