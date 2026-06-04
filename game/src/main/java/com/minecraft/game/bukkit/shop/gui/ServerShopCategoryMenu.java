package com.minecraft.game.bukkit.shop.gui;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.bukkit.shop.ShopEconomy;
import com.minecraft.game.bukkit.shop.catalog.ShopCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ServerShopCategoryMenu extends Menu {

    private final Account account;
    private final ShopCategory category;

    public ServerShopCategoryMenu(Player player, Account account, Menu parent, ShopCategory category) {
        super(player, "§8§lLoja §7| " + category.displayName(), 6, parent);
        this.account = account;
        this.category = category;
    }

    @Override
    public void build() {
        clear();
        int slot = 10;
        String sym = ShopEconomy.symbol();
        for (var product : category.products()) {
            if (slot > 43 || slot % 9 == 0 || slot % 9 == 8) {
                slot++;
                if (slot % 9 == 0 || slot % 9 == 8) slot++;
            }
            int s = slot++;
            set(s, MenuItem.of(product.material(), product.amount())
                    .name("§f" + format(product.material()))
                    .lore(
                            "§7Quantidade: §f" + product.amount(),
                            "§7Preco: §a" + sym + product.price(),
                            "",
                            "§eClique para comprar"
                    )
                    .click(p -> buy(p, product.material(), product.amount(), product.price())));
        }
        back(49);
    }

    private void buy(Player p, Material mat, int amount, int price) {
        Account acc = ShopEconomy.account(p);
        if (acc == null) {
            p.sendMessage("§cConta nao carregada.");
            return;
        }
        if (!ShopEconomy.charge(acc, price)) {
            p.sendMessage("§cSaldo insuficiente.");
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
        p.sendMessage("§aComprou §f" + amount + "x " + format(mat) + " §apor §a" + ShopEconomy.symbol() + price);
    }

    private static String format(Material mat) {
        return mat.name().toLowerCase().replace('_', ' ');
    }
}
