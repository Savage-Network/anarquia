package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.annotation.Completer;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.shop.SignShopManager;
import com.minecraft.game.util.TabCompleteUtil;
import com.minecraft.game.bukkit.shop.catalog.SellCatalog;
import com.minecraft.game.bukkit.shop.catalog.ServerShopCatalog;
import com.minecraft.game.bukkit.shop.gui.ShopHubMenu;
import org.bukkit.entity.Player;

import java.util.List;

public final class ShopCommand implements CommandInheritor {

    @Command(name = "loja", aliases = {"shop", "mercado"}, playerOnly = true,
            description = "Central de economia e lojas", usage = "/loja [gui|placa|reload]")
    public void execute(BukkitCommandContext ctx) {
        Account account = ctx.account().orElse(null);
        if (account == null) { ctx.reply("§cConta nao carregada."); return; }
        Player player = ctx.player();

        if (ctx.getArgs().length == 0 || ctx.getArgs()[0].equalsIgnoreCase("gui")
                || ctx.getArgs()[0].equalsIgnoreCase("menu")) {
            new ShopHubMenu(player, account).open();
            return;
        }

        if (ctx.getArgs()[0].equalsIgnoreCase("placa") || ctx.getArgs()[0].equalsIgnoreCase("sign")) {
            SignShopManager.sendHelp(player);
            return;
        }

        if (ctx.getArgs()[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("game.admin")) {
                ctx.reply("§cSem permissao.");
                return;
            }
            reloadEconomy(player);
            ctx.reply("§aEconomia recarregada (lojas, venda, leilao, placas).");
            return;
        }

        if (ctx.getArgs()[0].equalsIgnoreCase("ajuda") || ctx.getArgs()[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return;
        }

        new ShopHubMenu(player, account).open();
    }

    private static void sendHelp(Player player) {
        player.sendMessage("§6§lSavageMC §7| §fMercado");
        player.sendMessage("§f/loja §7- menu central (baú)");
        player.sendMessage("§f/sell §7- vender itens ao servidor");
        player.sendMessage("§f/ah §7- leilao entre jogadores");
        player.sendMessage("§f/trade <jogador> §7- troca segura");
        player.sendMessage("§f/loja placa §7- tutorial chest shop");
    }

    private static void reloadEconomy(Player player) {
        var plugin = com.minecraft.game.GamePlugin.getInstance();
        ServerShopCatalog.load(plugin);
        SellCatalog.load(plugin);
        if (Core.getSignShopData() != null) Core.getSignShopData().loadAll();
        if (Core.getAuctionData() != null) Core.getAuctionData().loadAll();
        com.minecraft.game.bukkit.shop.auction.AuctionManager.configure(
                com.minecraft.game.GameSettings.AUCTION_MAX_LISTINGS,
                com.minecraft.game.GameSettings.AUCTION_DURATION_HOURS,
                com.minecraft.game.GameSettings.AUCTION_TAX_PERCENT,
                com.minecraft.game.GameSettings.AUCTION_LISTING_FEE
        );
    }

    @Completer(name = "loja", subCommands = {"shop", "mercado"})
    public List<String> tab(BukkitCommandContext ctx) {
        if (ctx.getArgs().length == 1) {
            return TabCompleteUtil.filter(ctx.getArgs()[0],
                    "gui", "menu", "placa", "sign", "ajuda", "help", "reload");
        }
        return List.of();
    }
}
