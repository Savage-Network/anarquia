package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.vipkey.VipKeyService;

public final class ResgatarCommand implements CommandInheritor {

    @Command(name = "resgatar", aliases = {"redeem", "resgate", "key"},
            playerOnly = true,
            description = "Resgata uma key de VIP",
            usage = "/resgatar <codigo>")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) {
            ctx.replyKey("errors.account-not-loaded");
            return;
        }

        if (ctx.getArgs().length == 0) {
            ctx.reply("§cUso: /resgatar <codigo>");
            ctx.reply("§7Ex.: /resgatar 2BL3-M3BI-G2FY");
            return;
        }

        String code = String.join("", ctx.getArgs());
        if (code.isEmpty()) {
            ctx.reply("§cInforme o codigo da key.");
            return;
        }

        ctx.reply(VipKeyService.redeem(account, code));
    }
}
