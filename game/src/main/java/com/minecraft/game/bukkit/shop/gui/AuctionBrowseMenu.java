package com.minecraft.game.bukkit.shop.gui;

import com.minecraft.game.account.Account;
import com.minecraft.game.api.auction.AuctionListing;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.bukkit.shop.ShopEconomy;
import com.minecraft.game.bukkit.shop.auction.AuctionManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public final class AuctionBrowseMenu extends Menu {

    private final Account account;
    private final int page;

    public AuctionBrowseMenu(Player player, Account account, Menu parent, int page) {
        super(player, "§8§lLeilao §7| §fPag " + (page + 1), 6, parent);
        this.account = account;
        this.page = page;
    }

    @Override
    public void build() {
        clear();
        List<AuctionListing> all = AuctionManager.data().active();
        int perPage = 28;
        int start = page * perPage;
        int slot = 10;

        for (int i = start; i < Math.min(start + perPage, all.size()); i++) {
            if (slot > 43) break;
            if (slot % 9 == 0 || slot % 9 == 8) slot++;
            AuctionListing l = all.get(i);
            int s = slot++;
            set(s, MenuItem.of(l.getMaterial(), l.getAmount())
                    .name("§f" + l.getMaterial().name())
                    .lore(
                            "§7Vendedor: §f" + l.getSellerName(),
                            "§7Preco: §a" + ShopEconomy.symbol() + l.getPrice(),
                            "§7Qtd: §f" + l.getAmount(),
                            "",
                            "§eClique para comprar"
                    )
                    .click(p -> {
                        p.sendMessage(AuctionManager.buy(p, l.getId()));
                        reopen();
                    }));
        }

        if (start + perPage < all.size()) {
            set(53, MenuItem.of(Material.ARROW)
                    .name("§aProxima pagina")
                    .click(p -> new AuctionBrowseMenu(p, account, parent, page + 1).open()));
        }
        if (page > 0) {
            set(45, MenuItem.of(Material.ARROW)
                    .name("§aPagina anterior")
                    .click(p -> new AuctionBrowseMenu(p, account, parent, page - 1).open()));
        }

        set(49, MenuItem.of(Material.WRITABLE_BOOK)
                .name("§eMeus anuncios")
                .click(p -> new AuctionMyMenu(p, account, this).open()));

        back(48);
    }
}

final class AuctionMyMenu extends Menu {

    private final Account account;

    AuctionMyMenu(Player player, Account account, Menu parent) {
        super(player, "§8§lLeilao §7| §fMeus itens", 5, parent);
        this.account = account;
    }

    @Override
    public void build() {
        clear();
        var list = AuctionManager.data().bySeller(player.getUniqueId());
        int slot = 10;
        for (AuctionListing l : list) {
            set(slot++, MenuItem.of(l.getMaterial(), l.getAmount())
                    .name("§f" + l.getMaterial().name())
                    .lore("§7Preco: §a" + ShopEconomy.symbol() + l.getPrice(), "§cClique para cancelar")
                    .click(p -> {
                        p.sendMessage(AuctionManager.cancel(p, l.getId()));
                        open();
                    }));
        }
        if (list.isEmpty()) {
            set(22, MenuItem.of(Material.BARRIER).name("§7Nenhum anuncio ativo"));
        }
        back(40);
    }
}
