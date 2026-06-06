package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.util.TabCompleteUtil;
import com.minecraft.game.bukkit.shop.gui.AuctionBrowseMenu;
import com.minecraft.game.bukkit.shop.gui.ShopHubMenu;
import com.minecraft.game.bukkit.shop.auction.AuctionManager;
import org.bukkit.entity.Player;

import java.util.List;

public final class AuctionCommand implements CommandInheritor {

    @Command(name = "ah", aliases = {"leilao", "auction"}, playerOnly = true,
            description = "Leilao entre jogadores", usage = "/ah [vender <preco>|gui]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.replyKey("errors.account-not-loaded"); return; }
        Player player = ctx.player();

        if (ctx.getArgs().length == 0) {
            new AuctionBrowseMenu(player, account, new ShopHubMenu(player, account), 0).open();
            return;
        }

        if (ctx.getArgs()[0].equalsIgnoreCase("gui")) {
            new AuctionBrowseMenu(player, account, new ShopHubMenu(player, account), 0).open();
            return;
        }

        if (ctx.getArgs()[0].equalsIgnoreCase("vender") || ctx.getArgs()[0].equalsIgnoreCase("sell")) {
            if (ctx.getArgs().length < 2) {
                ctx.reply("§cUso: /ah vender <preco>");
                return;
            }
            try {
                int price = Integer.parseInt(ctx.getArgs()[1]);
                ctx.reply(AuctionManager.listHand(player, price));
            } catch (NumberFormatException e) {
                ctx.reply("§cPreco invalido.");
            }
            return;
        }

        ctx.reply("§7/ah §f- abrir leilao");
        ctx.reply("§7/ah vender <preco> §f- anunciar item na mao");
    }

    @Completer(name = "ah", subCommands = {"leilao", "auction"})
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.filter(ctx.getArgs()[0], "gui", "vender", "sell");
        }
        if (ctx.getArgs().length == 2) {
            String sub = ctx.getArgs()[0].toLowerCase();
            if (sub.equals("vender") || sub.equals("sell")) {
                return TabCompleteUtil.filter(ctx.getArgs()[1], "100", "500", "1000", "5000", "10000");
            }
        }
        return List.of();
    }
}
