package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.vanish.VanishService;

/**
 * /go — atalho de vanish para staff.
 */
public final class GoCommand implements CommandInheritor {

    @Command(name = "go", playerOnly = true, permission = "game.go",
            rank = RankType.HELPER, description = "Atalho de vanish (staff)")
    public void go(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta não carregada."); return; }
        if (!VanishService.canVanish(account)) { ctx.reply("§cSem permissão."); return; }
        VanishService.toggle(ctx.player());
    }
}
