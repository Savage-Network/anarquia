package com.minecraft.game.bukkit.shop.gui;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.bukkit.shop.ItemDisplayNames;
import com.minecraft.game.bukkit.shop.ShopEconomy;
import com.minecraft.game.bukkit.shop.catalog.ShopCategory;
import com.minecraft.game.bukkit.shop.catalog.ShopProduct;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class ServerShopCategoryMenu extends Menu {

    private final Account account;
    private final ShopCategory category;
    private final int page;

    public ServerShopCategoryMenu(Player player, Account account, Menu parent, ShopCategory category) {
        this(player, account, parent, category, 0);
    }

    public ServerShopCategoryMenu(Player player, Account account, Menu parent, ShopCategory category, int page) {
        super(player, "§8§lLoja §7| " + category.displayName(), 6, parent);
        this.account = account;
        this.category = category;
        this.page = Math.max(0, page);
    }

    @Override
    public void build() {
        clear();
        String sym = ShopEconomy.symbol();
        List<ShopProduct> products = category.products();
        int pages = ShopMenuLayout.totalPages(products.size());

        ShopMenuLayout.header(
                this, account, category.icon(),
                category.displayName(),
                "§8▪ §7Itens: §f" + products.size(),
                "§8▪ §7Pagina: §f" + (page + 1) + "§7/§f" + pages,
                "",
                "§7Clique para comprar o pacote"
        );

        int start = page * ShopMenuLayout.PER_PAGE;
        int[] slots = ShopMenuLayout.contentSlots();
        for (int i = 0; i < slots.length; i++) {
            int index = start + i;
            if (index >= products.size()) break;

            ShopProduct product = products.get(index);
            int slot = slots[i];
            set(slot, MenuItem.of(product.material(), product.amount())
                    .name(ItemDisplayNames.menuTitle(product.material(), product.amount()))
                    .lore(
                            "§8▪ §7Pacote: §f" + product.amount() + " un.",
                            "§8▪ §7Preco: §a" + sym + product.price(),
                            "§8▪ §7Saldo: §a" + sym + account.getGolds(),
                            account.getGolds() >= product.price()
                                    ? "§8▪ §7Status: §aPode comprar"
                                    : "§8▪ §7Status: §cSaldo insuficiente",
                            "",
                            "§e§lClique §7para comprar"
                    )
                    .click(p -> buy(p, product.material(), product.amount(), product.price())));
        }

        ShopMenuLayout.fillContentBackground(this);
        ShopMenuLayout.pageArrows(this, account, parent, page, products.size(),
                (p, acc, par, pg) -> new ServerShopCategoryMenu(p, acc, par, category, pg).open(),
                (p, acc, par, pg) -> new ServerShopCategoryMenu(p, acc, par, category, pg).open());
        ShopMenuLayout.bottomBar(this, account, parent, ShopMenuLayout.BottomBarOptions.category(parent));
    }

    private void buy(Player p, Material mat, int amount, int price) {
        Account acc = ShopEconomy.account(p);
        if (acc == null) {
            p.sendMessage("§cConta nao carregada.");
            return;
        }
        if (!ShopEconomy.charge(acc, price)) {
            p.sendMessage("§cSaldo insuficiente. Preco: §a" + ShopEconomy.symbol() + price);
            clickSound();
            return;
        }
        if (!ShopEconomy.giveItems(p, new ItemStack(mat, amount))) {
            acc.addGolds(price);
            p.sendMessage("§cInventario cheio.");
            return;
        }
        acc.markDirty();
        successSound();
        p.sendMessage(ItemDisplayNames.buyMessage(amount, mat, "§a" + ShopEconomy.symbol() + price));
        reopen();
    }
}
