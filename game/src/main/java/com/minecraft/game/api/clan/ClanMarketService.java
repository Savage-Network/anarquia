package com.minecraft.game.api.clan;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.backend.data.ClanMarketData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public final class ClanMarketService {

    private static final int MAX_LISTINGS = 18;

    private ClanMarketService() {}

    public static ClanService.Result listItem(Account seller, ItemStack stack, int price) {
        if (seller == null || !seller.hasClan()) return ClanService.Result.fail("§cSem clã.");
        if (stack == null || stack.getType().isAir() || price <= 0) {
            return ClanService.Result.fail("§cItem ou preço inválido.");
        }
        ClanMarketData data = Core.getClanMarketData();
        if (data == null) return ClanService.Result.fail("§cMercado indisponível.");

        Clan clan = seller.getClan();
        if (data.countByClan(clan.getId()) >= MAX_LISTINGS) {
            return ClanService.Result.fail("§cLimite de anúncios do clã atingido.");
        }

        Player player = seller.player();
        if (player == null) return ClanService.Result.fail("§cJogador offline.");

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType().isAir()) {
            return ClanService.Result.fail("§cSegure o item na mão.");
        }

        int amount = hand.getAmount();
        Material material = hand.getType();
        hand.setAmount(0);

        ClanMarketData.Listing listing = new ClanMarketData.Listing(
                UUID.randomUUID().toString(),
                clan.getId(),
                seller.getId(),
                seller.getNickname(),
                material.name(),
                amount,
                price,
                System.currentTimeMillis());
        data.insert(listing);
        clan.broadcast("§7" + seller.getNickname() + " listou §f" + amount + "x "
                + material.name() + " §7por §a" + price);
        return ClanService.Result.ok("§aItem listado no mercado do clã.");
    }

    public static ClanService.Result buy(Account buyer, String listingId) {
        if (buyer == null || !buyer.hasClan()) return ClanService.Result.fail("§cSem clã.");
        ClanMarketData data = Core.getClanMarketData();
        if (data == null) return ClanService.Result.fail("§cMercado indisponível.");

        var opt = data.find(listingId);
        if (opt.isEmpty()) return ClanService.Result.fail("§cAnúncio não encontrado.");
        ClanMarketData.Listing listing = opt.get();
        if (!listing.clanId().equals(buyer.getClan().getId())) {
            return ClanService.Result.fail("§cAnúncio de outro clã.");
        }
        if (listing.sellerId().equals(buyer.getId())) {
            return ClanService.Result.fail("§cNão pode comprar seu próprio item.");
        }
        if (!buyer.removeGolds(listing.price())) {
            return ClanService.Result.fail("§cSaldo insuficiente.");
        }

        Player player = buyer.player();
        if (player == null) {
            buyer.addGolds(listing.price());
            return ClanService.Result.fail("§cJogador offline.");
        }

        Material mat = Material.matchMaterial(listing.material());
        if (mat != null) {
            player.getInventory().addItem(new ItemStack(mat, listing.amount()));
        }

        Account sellerAcc = Core.getAccountController().of(listing.sellerId());
        if (sellerAcc != null) sellerAcc.addGolds(listing.price());

        data.delete(listing.id());
        buyer.getClan().broadcast("§7" + buyer.getNickname() + " comprou §f"
                + listing.amount() + "x " + listing.material() + " §7de §f" + listing.sellerName());
        return ClanService.Result.ok("§aCompra concluída!");
    }

    public static List<ClanMarketData.Listing> listings(UUID clanId) {
        ClanMarketData data = Core.getClanMarketData();
        if (data == null) return List.of();
        return data.listByClan(clanId, 54);
    }
}
