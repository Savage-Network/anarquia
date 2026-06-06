package com.minecraft.game.bukkit.shop.gui;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.bukkit.shop.ItemDisplayNames;
import com.minecraft.game.bukkit.shop.ShopEconomy;
import com.minecraft.game.bukkit.shop.catalog.SellCatalog;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class SellMenu extends Menu {

    private final Account account;
    private final int page;

    public SellMenu(Player player, Account account, Menu parent) {
        this(player, account, parent, 0);
    }

    public SellMenu(Player player, Account account, Menu parent, int page) {
        super(player, buildTitle(page), 6, parent);
        this.account = account;
        this.page = Math.max(0, page);
    }

    private static String buildTitle(int page) {
        return "§8§lVenda §7| §fPag " + (page + 1);
    }

    @Override
    public void build() {
        clear();
        String sym = ShopEconomy.symbol();
        List<Material> materials = SellCatalog.materials();
        int pages = ShopMenuLayout.totalPages(materials.size());

        ShopMenuLayout.header(
                this, account, Material.GOLD_BLOCK,
                "§6§lVenda §8| §fSavageMC",
                "§8▪ §7Itens aceitos: §f" + materials.size(),
                "§8▪ §7Pagina: §f" + (page + 1) + "§7/§f" + pages,
                "",
                "§7Clique nos itens para vender"
        );

        set(2, MenuItem.of(Material.GOLD_NUGGET)
                .name("§6§lVender mao")
                .lore(
                        "§7Vende o item na sua mao",
                        "§7pelo preco cadastrado",
                        "",
                        "§eClique"
                )
                .click(this::sellHand));

        set(6, MenuItem.of(Material.CHEST)
                .name("§e§lVender inventario")
                .lore(
                        "§7Vende todos os itens",
                        "§7com preco no servidor",
                        "",
                        "§eClique"
                )
                .click(this::sellAll));

        int start = page * ShopMenuLayout.PER_PAGE;
        int[] slots = ShopMenuLayout.contentSlots();
        for (int i = 0; i < slots.length; i++) {
            int index = start + i;
            if (index >= materials.size()) break;

            Material mat = materials.get(index);
            int unit = SellCatalog.price(mat);
            int owned = SellCatalog.countOwned(player, mat);
            int slot = slots[i];

            set(slot, MenuItem.of(mat)
                    .name(ItemDisplayNames.menuTitle(mat))
                    .lore(
                            "§8▪ §7Preco: §a" + sym + unit + " §7/un",
                            "§8▪ §7No inventario: §f" + owned,
                            owned > 0
                                    ? "§8▪ §7Total: §a" + sym + (unit * owned)
                                    : "§8▪ §7Total: §c0",
                            "",
                            owned > 0
                                    ? "§e§lClique §7para vender tudo"
                                    : "§cSem estoque"
                    )
                    .click(p -> sellMaterial(p, mat)));
        }

        ShopMenuLayout.fillContentBackground(this);
        ShopMenuLayout.pageArrows(this, account, parent, page, materials.size(),
                SellMenu::openPage, SellMenu::openPage);
        ShopMenuLayout.bottomBar(this, account, parent, ShopMenuLayout.BottomBarOptions.sell(parent));
    }

    private static void openPage(Player player, Account account, Menu parent, int page) {
        new SellMenu(player, account, parent, page).open();
    }

    private void sellMaterial(Player p, Material mat) {
        int owned = SellCatalog.countOwned(p, mat);
        if (owned <= 0) {
            p.sendMessage("§cVoce nao tem " + ItemDisplayNames.plain(mat) + " para vender.");
            clickSound();
            return;
        }
        int total = SellCatalog.sellMaterial(p, mat);
        if (total <= 0) {
            p.sendMessage("§cEste item nao pode ser vendido.");
            clickSound();
            return;
        }
        pay(p, total, owned, mat);
        reopen();
    }

    private void sellHand(Player p) {
        ItemStack hand = p.getInventory().getItemInMainHand();
        int total = SellCatalog.sellStack(hand);
        if (total <= 0) {
            p.sendMessage("§cEste item nao pode ser vendido.");
            clickSound();
            return;
        }
        int amount = hand.getAmount();
        Material mat = hand.getType();
        hand.setAmount(0);
        p.getInventory().setItemInMainHand(null);
        pay(p, total, amount, mat);
        reopen();
    }

    private void sellAll(Player p) {
        int total = 0;
        for (ItemStack stack : p.getInventory().getContents()) {
            if (stack == null) continue;
            int val = SellCatalog.sellStack(stack);
            if (val > 0) {
                total += val;
                stack.setAmount(0);
            }
        }
        if (total <= 0) {
            p.sendMessage("§cNada para vender.");
            clickSound();
            return;
        }
        pay(p, total, -1, null);
        reopen();
    }

    private void pay(Player p, int total, int amount, Material mat) {
        Account acc = ShopEconomy.account(p);
        if (acc == null) return;
        ShopEconomy.pay(acc, total);
        successSound();
        if (amount > 0 && mat != null) {
            p.sendMessage("§aVendeu §f" + amount + "x "
                    + ItemDisplayNames.plain(mat) + " §apor §f" + ShopEconomy.symbol() + total);
        } else {
            p.sendMessage("§aVendeu itens por §f" + ShopEconomy.symbol() + total);
        }
    }
}
