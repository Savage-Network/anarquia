package com.minecraft.game.bukkit.shop.gui;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.bukkit.shop.ShopEconomy;
import com.minecraft.game.bukkit.shop.catalog.SellCatalog;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class SellMenu extends Menu {

    private final Account account;

    public SellMenu(Player player, Account account, Menu parent) {
        super(player, "§8§lVenda §7| §fServidor", 5, parent);
        this.account = account;
    }

    @Override
    public void build() {
        clear();
        String sym = ShopEconomy.symbol();

        set(11, MenuItem.of(Material.GOLD_NUGGET)
                .name("§6§lVender mao")
                .lore("§7Vende o item na sua mao", "", "§eClique")
                .click(p -> sellHand(p)));

        set(15, MenuItem.of(Material.CHEST)
                .name("§e§lVender inventario")
                .lore("§7Vende tudo com preco", "§7cadastrado no servidor", "", "§eClique")
                .click(p -> sellAll(p)));

        set(22, MenuItem.of(Material.BOOK)
                .name("§7Precos de referencia")
                .lore(
                        "§8DIAMOND §7= §a" + sym + SellCatalog.price(Material.DIAMOND) + "/un",
                        "§8IRON_INGOT §7= §a" + sym + SellCatalog.price(Material.IRON_INGOT) + "/un",
                        "§8COBBLESTONE §7= §a" + sym + SellCatalog.price(Material.COBBLESTONE) + "/un",
                        "",
                        "§7Edite sell-prices.yml"
                ));

        back(49);
    }

    private void sellHand(Player p) {
        ItemStack hand = p.getInventory().getItemInMainHand();
        int total = SellCatalog.sellStack(hand);
        if (total <= 0) {
            p.sendMessage("§cEste item nao pode ser vendido.");
            return;
        }
        hand.setAmount(0);
        p.getInventory().setItemInMainHand(null);
        pay(p, total);
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
            return;
        }
        pay(p, total);
    }

    private void pay(Player p, int total) {
        Account acc = ShopEconomy.account(p);
        if (acc == null) return;
        ShopEconomy.pay(acc, total);
        successSound();
        p.sendMessage("§aVendeu itens por §f" + ShopEconomy.symbol() + total);
    }
}
