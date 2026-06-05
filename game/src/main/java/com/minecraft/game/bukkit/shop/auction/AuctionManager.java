package com.minecraft.game.bukkit.shop.auction;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.auction.AuctionListing;
import com.minecraft.game.backend.data.AuctionData;
import com.minecraft.game.bukkit.shop.ItemDisplayNames;
import com.minecraft.game.bukkit.shop.ShopEconomy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class AuctionManager {

    private static int maxListings = 5;
    private static long durationMs = TimeUnit.HOURS.toMillis(48);
    private static int taxPercent = 5;
    private static int listingFee = 50;

    private AuctionManager() {}

    public static void configure(int max, int hours, int tax, int fee) {
        maxListings = max;
        durationMs = TimeUnit.HOURS.toMillis(hours);
        taxPercent = tax;
        listingFee = fee;
    }

    public static AuctionData data() {
        return Core.getAuctionData();
    }

    public static String listHand(Player player, int price) {
        Account acc = ShopEconomy.account(player);
        if (acc == null) return "§cConta nao carregada.";

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) return "§cSegure o item na mao.";

        if (data().bySeller(player.getUniqueId()).size() >= maxListings) {
            return "§cLimite de anuncios: " + maxListings;
        }

        if (price <= 0) return "§cPreco invalido.";
        if (!ShopEconomy.charge(acc, listingFee)) {
            return "§cTaxa de anuncio: " + ShopEconomy.symbol() + listingFee;
        }

        hand.setAmount(1);
        player.getInventory().setItemInMainHand(null);

        long now = System.currentTimeMillis();
        AuctionListing listing = new AuctionListing(
                UUID.randomUUID(),
                player.getUniqueId(),
                player.getName(),
                hand.getType(),
                hand.getAmount(),
                price,
                now,
                now + durationMs
        );
        data().save(listing);
        acc.markDirty();
        return "§aAnunciou §f" + ItemDisplayNames.plain(hand) + " §apor §f" + ShopEconomy.symbol() + price;
    }

    public static String buy(Player buyer, UUID listingId) {
        Account buyerAcc = ShopEconomy.account(buyer);
        if (buyerAcc == null) return "§cConta nao carregada.";

        AuctionListing listing = data().get(listingId).orElse(null);
        if (listing == null) return "§cAnuncio expirado ou inexistente.";
        if (listing.getSellerId().equals(buyer.getUniqueId())) return "§cVoce nao pode comprar seu item.";

        int tax = ShopEconomy.tax(listing.getPrice(), taxPercent);
        int total = listing.getPrice() + tax;
        if (!ShopEconomy.charge(buyerAcc, total)) {
            return "§cSaldo insuficiente (§f" + ShopEconomy.symbol() + total + "§c).";
        }

        ItemStack stack = new ItemStack(listing.getMaterial(), listing.getAmount());
        if (!ShopEconomy.giveItems(buyer, stack)) {
            buyerAcc.addGolds(total);
            return "§cInventario cheio.";
        }

        Account sellerAcc = Core.getAccountController().of(listing.getSellerId());
        if (sellerAcc != null) {
            ShopEconomy.pay(sellerAcc, listing.getPrice());
        }

        data().delete(listingId);
        buyerAcc.markDirty();
        return ItemDisplayNames.buyMessage(listing.getAmount(), listing.getMaterial(),
                "§f" + ShopEconomy.symbol() + total);
    }

    public static String cancel(Player player, UUID listingId) {
        AuctionListing listing = data().get(listingId).orElse(null);
        if (listing == null) return "§cAnuncio nao encontrado.";
        if (!listing.getSellerId().equals(player.getUniqueId()) && !player.hasPermission("game.admin")) {
            return "§cEste anuncio nao e seu.";
        }

        ItemStack stack = new ItemStack(listing.getMaterial(), listing.getAmount());
        var left = player.getInventory().addItem(stack);
        if (!left.isEmpty()) return "§cInventario cheio.";

        data().delete(listingId);
        return "§eAnuncio cancelado.";
    }
}
