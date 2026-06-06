package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.shop.gui.SellMenu;
import com.minecraft.game.bukkit.shop.gui.ShopHubMenu;
import org.bukkit.entity.Player;

public final class SellCommand implements CommandInheritor {

    @Command(name = "sell", aliases = {"vender", "venda"}, playerOnly = true,
            description = "Vende itens ao servidor", usage = "/sell")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.replyKey("errors.account-not-loaded"); return; }
        new SellMenu(ctx.player(), account, new ShopHubMenu(ctx.player(), account)).open();
    }
}
