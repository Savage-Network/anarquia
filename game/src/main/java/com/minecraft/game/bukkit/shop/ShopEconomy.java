package com.minecraft.game.bukkit.shop;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ShopEconomy {

    private ShopEconomy() {}

    public static String symbol() {
        return SemiAnarchyConfig.currencySymbol();
    }

    public static Account account(Player player) {
        return Core.getAccountController().of(player.getUniqueId());
    }

    public static boolean canAfford(Account acc, int price) {
        return acc != null && acc.getGolds() >= price;
    }

    public static boolean charge(Account acc, int price) {
        if (acc == null || price <= 0) return false;
        return acc.removeGolds(price);
    }

    public static void pay(Account acc, int amount) {
        if (acc != null && amount > 0) {
            acc.addGolds(amount);
            acc.markDirty();
        }
    }

    public static int tax(int amount, int percent) {
        return (amount * percent) / 100;
    }

    public static boolean giveItems(Player player, ItemStack stack) {
        var leftover = player.getInventory().addItem(stack.clone());
        return leftover.isEmpty();
    }
}
