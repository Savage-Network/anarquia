package com.minecraft.game.bukkit.shop.gui;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.bukkit.shop.SignShopManager;
import com.minecraft.game.message.MenuTexts;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class ShopHubMenu extends Menu {

    private final Account account;

    public ShopHubMenu(Player player, Account account) {
        this(player, account, null);
    }

    public ShopHubMenu(Player player, Account account, Menu parent) {
        super(player, MenuTexts.title(player, "menus.shop.hub-title"), 5, parent);
        this.account = account;
    }

    @Override
    public void build() {
        clear();

        ShopMenuLayout.header(
                this, account, Material.NETHER_STAR,
                MenuTexts.legacy(player, "menus.shop.hub-header"),
                MenuTexts.legacy(player, "menus.shop.hub-sub"),
                "",
                MenuTexts.legacy(player, "menus.shop.hub-desc")
        );

        set(20, MenuItem.of(Material.EMERALD)
                .name(MenuTexts.legacy(player, "menus.shop.server-shop"))
                .lore(
                        MenuTexts.legacy(player, "menus.shop.server-shop-lore-1"),
                        MenuTexts.legacy(player, "menus.shop.server-shop-lore-2"),
                        "",
                        MenuTexts.legacy(player, "menus.common.click-open")
                )
                .glow()
                .click(p -> new ServerShopMenu(p, account, this).open()));

        set(22, MenuItem.of(Material.GOLD_INGOT)
                .name(MenuTexts.legacy(player, "menus.shop.sell"))
                .lore(
                        MenuTexts.legacy(player, "menus.shop.sell-lore-1"),
                        MenuTexts.legacy(player, "menus.shop.sell-lore-2"),
                        "",
                        MenuTexts.legacy(player, "menus.common.click-open")
                )
                .glow()
                .click(p -> new SellMenu(p, account, this).open()));

        set(24, MenuItem.of(Material.ENDER_CHEST)
                .name(MenuTexts.legacy(player, "menus.shop.auction"))
                .lore(
                        MenuTexts.legacy(player, "menus.shop.auction-lore-1"),
                        MenuTexts.legacy(player, "menus.shop.auction-lore-2"),
                        "",
                        MenuTexts.legacy(player, "menus.common.click-open")
                )
                .click(p -> new AuctionBrowseMenu(p, account, this, 0).open()));

        set(30, MenuItem.of(Material.OAK_SIGN)
                .name(MenuTexts.legacy(player, "menus.shop.sign-shop"))
                .lore(
                        MenuTexts.legacy(player, "menus.shop.sign-shop-lore-1"),
                        MenuTexts.legacy(player, "menus.shop.sign-shop-lore-2"),
                        "",
                        MenuTexts.legacy(player, "menus.common.click-open")
                )
                .click(SignShopManager::sendHelp));

        set(32, MenuItem.of(Material.PLAYER_HEAD)
                .name(MenuTexts.legacy(player, "menus.shop.trade"))
                .lore(
                        MenuTexts.legacy(player, "menus.shop.trade-lore-1"),
                        MenuTexts.legacy(player, "menus.shop.trade-lore-2"),
                        "",
                        MenuTexts.legacy(player, "menus.common.click-select")
                )
                .click(p -> p.sendMessage(MenuTexts.legacy(p, "menus.shop.trade-hint"))));

        fillIfEmpty(3, MenuItem.of(Material.LIME_STAINED_GLASS_PANE).name(" "));
        fillIfEmpty(5, MenuItem.of(Material.LIME_STAINED_GLASS_PANE).name(" "));
        fillIfEmpty(21, MenuItem.of(Material.YELLOW_STAINED_GLASS_PANE).name(" "));
        fillIfEmpty(23, MenuItem.of(Material.YELLOW_STAINED_GLASS_PANE).name(" "));

        closeButton(40);
    }
}
