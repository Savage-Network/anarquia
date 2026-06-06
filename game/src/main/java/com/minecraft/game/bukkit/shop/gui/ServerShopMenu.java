package com.minecraft.game.bukkit.shop.gui;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.bukkit.shop.ShopEconomy;
import com.minecraft.game.bukkit.shop.catalog.ServerShopCatalog;
import com.minecraft.game.bukkit.shop.catalog.ShopCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class ServerShopMenu extends Menu {

    private final Account account;

    public ServerShopMenu(Player player, Account account, Menu parent) {
        super(player, "§8§lLoja §7| §fSavageMC", 6, parent);
        this.account = account;
    }

    @Override
    public void build() {
        clear();
        List<ShopCategory> categories = new ArrayList<>(ServerShopCatalog.categories().values());

        ShopMenuLayout.header(
                this, account, Material.EMERALD_BLOCK,
                "§a§lLoja do Servidor",
                "§8▪ §7Categorias: §f" + categories.size(),
                "",
                "§7Compre no spawn protegido"
        );

        int[] slots = ShopMenuLayout.contentSlots();
        int limit = Math.min(slots.length, categories.size());
        for (int i = 0; i < limit; i++) {
            ShopCategory cat = categories.get(i);
            int slot = slots[i];
            set(slot, MenuItem.of(cat.icon())
                    .name(cat.displayName())
                    .lore(
                            "§8▪ §7Itens: §f" + cat.products().size(),
                            "",
                            "§eClique para abrir"
                    )
                    .click(p -> new ServerShopCategoryMenu(p, account, this, cat).open()));
        }

        if (categories.isEmpty()) {
            set(22, MenuItem.of(Material.BARRIER)
                    .name("§cNenhuma categoria")
                    .lore("§7Edite §fshops.yml §7ou use", "§f/loja reload"));
        }

        ShopMenuLayout.fillContentBackground(this);
        ShopMenuLayout.bottomBar(this, account, parent, ShopMenuLayout.BottomBarOptions.shop(parent));
    }
}
