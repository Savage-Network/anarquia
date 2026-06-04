package com.minecraft.game.bukkit.shop.gui;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.bukkit.shop.ShopEconomy;
import com.minecraft.game.bukkit.shop.catalog.ServerShopCatalog;
import com.minecraft.game.bukkit.shop.catalog.ShopCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
public final class ServerShopMenu extends Menu {

    private final Account account;

    public ServerShopMenu(Player player, Account account, Menu parent) {
        super(player, "§8§lLoja §7| §fCategorias", 5, parent);
        this.account = account;
    }

    @Override
    public void build() {
        clear();
        for (ShopCategory cat : ServerShopCatalog.categories().values()) {
            set(cat.menuSlot(), MenuItem.of(cat.icon())
                    .name(cat.displayName())
                    .lore("§7" + cat.products().size() + " itens", "", "§eClique para ver")
                    .click(p -> new ServerShopCategoryMenu(p, account, this, cat).open()));
        }
        back(49);
    }
}
