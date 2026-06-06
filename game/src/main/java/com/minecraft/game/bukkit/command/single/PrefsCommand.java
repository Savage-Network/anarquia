package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.menu.prefs.PrefsMenu;

public final class PrefsCommand implements CommandInheritor {

    @Command(name = "prefs", aliases = {"preferencias"}, playerOnly = true,
            description = "Preferencias da conta (menu)",
            usage = "/prefs")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.replyKey("errors.account-not-loaded");
            return;
        }
        new PrefsMenu(ctx.player(), account).open();
    }
}
