package com.minecraft.game.bukkit.shop.gui;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.bukkit.shop.ShopEconomy;
import com.minecraft.game.message.MenuTexts;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Layout padrao dos menus de economia (loja / venda / mercado).
 */
public final class ShopMenuLayout {

    public static final int PER_PAGE = 28;

    private static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private ShopMenuLayout() {}

    public static int[] contentSlots() {
        return CONTENT_SLOTS;
    }

    public static int totalPages(int itemCount) {
        if (itemCount <= 0) return 1;
        return (itemCount + PER_PAGE - 1) / PER_PAGE;
    }

    public static void header(Menu menu, Account account, Material icon, String title, String... extraLore) {
        Player player = menu.getPlayer();
        String sym = ShopEconomy.symbol();
        MenuItem item = MenuItem.of(icon)
                .name(title)
                .lore(MenuTexts.format(player, "menus.common.balance",
                        MenuTexts.map("symbol", sym, "amount", String.valueOf(account.getGolds()))));
        for (String line : extraLore) {
            item = item.lore(line);
        }
        menu.place(4, item.glow());
    }

    /** Preenche slots de conteudo vazios com vidro escuro. */
    public static void fillContentBackground(Menu menu) {
        MenuItem pane = MenuItem.of(Material.BLACK_STAINED_GLASS_PANE).name(" ");
        for (int slot : CONTENT_SLOTS) {
            menu.placeIfEmpty(slot, pane);
        }
    }

    public static void pageArrows(Menu menu, Account account, Menu parent, int page, int itemCount,
                                  PageOpener prev, PageOpener next) {
        Player player = menu.getPlayer();
        int pages = totalPages(itemCount);
        int start = page * PER_PAGE;

        if (page > 0) {
            menu.place(45, MenuItem.of(Material.ARROW)
                    .name(MenuTexts.legacy(player, "menus.common.previous"))
                    .lore(MenuTexts.format(player, "menus.common.page",
                            MenuTexts.map("page", String.valueOf(page) + " / " + pages)))
                    .click(p -> prev.open(p, account, parent, page - 1)));
        }

        if (start + PER_PAGE < itemCount) {
            menu.place(53, MenuItem.of(Material.ARROW)
                    .name(MenuTexts.legacy(player, "menus.common.next"))
                    .lore(MenuTexts.format(player, "menus.common.page",
                            MenuTexts.map("page", String.valueOf(page + 2) + " / " + pages)))
                    .click(p -> next.open(p, account, parent, page + 1)));
        }
    }

    public static void bottomBar(Menu menu, Account account, Menu parent, BottomBarOptions options) {
        Player player = menu.getPlayer();
        if (parent != null) {
            menu.place(48, MenuItem.of(Material.ARROW)
                    .name(MenuTexts.legacy(player, "menus.common.back"))
                    .lore(MenuTexts.legacy(player, "menus.common.back-lore"))
                    .click(p -> parent.open()));
        }

        Menu hubParent = options.hubParent() != null ? options.hubParent() : parent;
        menu.place(49, MenuItem.of(Material.NETHER_STAR)
                .name(MenuTexts.legacy(player, "menus.shop.hub-header"))
                .lore(MenuTexts.legacy(player, "menus.shop.hub-desc"), "",
                        MenuTexts.legacy(player, "menus.common.click-open"))
                .click(p -> new ShopHubMenu(p, account, hubParent).open()));

        if (options.showShop()) {
            menu.place(50, MenuItem.of(Material.EMERALD)
                    .name(MenuTexts.legacy(player, "menus.shop.server-shop"))
                    .lore(MenuTexts.legacy(player, "menus.shop.server-shop-lore-1"), "",
                            MenuTexts.legacy(player, "menus.common.click-open"))
                    .click(p -> new ServerShopMenu(p, account, hubParent).open()));
        }

        if (options.showSell()) {
            menu.place(46, MenuItem.of(Material.GOLD_INGOT)
                    .name(MenuTexts.legacy(player, "menus.shop.sell"))
                    .lore(MenuTexts.legacy(player, "menus.shop.sell-lore-1"), "",
                            MenuTexts.legacy(player, "menus.common.click-open"))
                    .click(p -> new SellMenu(p, account, hubParent).open()));
        }

        menu.place(52, MenuItem.of(Material.BARRIER)
                .name(MenuTexts.legacy(player, "menus.common.close"))
                .click(Player::closeInventory));
    }

    @FunctionalInterface
    public interface PageOpener {
        void open(Player player, Account account, Menu parent, int page);
    }

    public record BottomBarOptions(boolean showShop, boolean showSell, Menu hubParent) {
        public static BottomBarOptions sell(Menu hubParent) {
            return new BottomBarOptions(true, false, hubParent);
        }

        public static BottomBarOptions shop(Menu hubParent) {
            return new BottomBarOptions(false, true, hubParent);
        }

        public static BottomBarOptions category(Menu hubParent) {
            return new BottomBarOptions(true, true, hubParent);
        }
    }
}
