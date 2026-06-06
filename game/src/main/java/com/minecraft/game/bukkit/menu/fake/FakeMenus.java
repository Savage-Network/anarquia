package com.minecraft.game.bukkit.menu.fake;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.fake.FakeService;
import com.minecraft.game.message.MenuTexts;
import com.minecraft.game.util.ModernText;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class FakeMenus {

    private static final NamespacedKey ACTION_KEY = new NamespacedKey(GamePlugin.getInstance(), "fake_action");

    private FakeMenus() {}

    public static void openMainMenu(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        FakeMenuHolder holder = new FakeMenuHolder("fake_hub");
        Inventory inv = Bukkit.createInventory(holder, 45,
                MenuTexts.component(player, "menus.fake.title"));
        holder.bind(inv);
        fillGlass(inv);

        boolean active = account.isFake();
        List<String> headerLore = new ArrayList<>();
        if (active) {
            headerLore.add(MenuTexts.format(player, "menus.fake.current",
                    MenuTexts.map("nick", account.getNickname())));
        } else {
            headerLore.add(MenuTexts.legacy(player, "menus.fake.real-identity"));
        }
        headerLore.add(MenuTexts.format(player, "menus.fake.real-name",
                MenuTexts.map("name", account.getRealName())));
        headerLore.add("");
        headerLore.add(MenuTexts.legacy(player, "menus.fake.disguise-desc"));
        headerLore.add(MenuTexts.legacy(player, "menus.fake.skin-auto"));

        inv.setItem(4, labeled(player, Material.NAME_TAG, MenuTexts.legacy(player, "menus.fake.header"), headerLore));

        inv.setItem(20, actionItem(player, Material.LIME_DYE, MenuTexts.legacy(player, "menus.fake.random"),
                List.of(
                        MenuTexts.legacy(player, "menus.fake.random-lore-1"),
                        MenuTexts.legacy(player, "menus.fake.random-lore-2"),
                        "",
                        MenuTexts.legacy(player, "menus.common.click-open")),
                "random"));

        inv.setItem(22, actionItem(player, Material.WRITABLE_BOOK, MenuTexts.legacy(player, "menus.fake.custom"),
                List.of(
                        MenuTexts.legacy(player, "menus.fake.custom-lore-1"),
                        MenuTexts.legacy(player, "menus.fake.custom-lore-2"),
                        "",
                        MenuTexts.legacy(player, "menus.fake.custom-example")),
                "help"));

        if (account.getContext().getFake().getLastNick() != null
                && !account.getContext().getFake().getLastNick().isBlank()) {
            inv.setItem(24, actionItem(player, Material.CLOCK, MenuTexts.legacy(player, "menus.fake.last"),
                    List.of(
                            MenuTexts.format(player, "menus.fake.last-lore",
                                    MenuTexts.map("nick", account.getContext().getFake().getLastNick())),
                            "",
                            MenuTexts.legacy(player, "menus.common.click-open")),
                    "last"));
        }

        if (active) {
            inv.setItem(40, actionItem(player, Material.BARRIER, MenuTexts.legacy(player, "menus.fake.remove"),
                    List.of(
                            MenuTexts.legacy(player, "menus.fake.remove-lore-1"),
                            "",
                            MenuTexts.legacy(player, "menus.fake.remove-lore-2")),
                    "off"));
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.4f);
    }

    public static void handleClick(Player player, FakeMenuHolder holder, ItemStack item) {
        if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        String action = getAction(item);
        if (action == null) return;

        switch (action) {
            case "random" -> {
                String nick = FakeService.randomNick();
                FakeService.enable(account, player, nick,
                        msg -> {
                            player.sendMessage(msg);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                            openMainMenu(player);
                        },
                        player::sendMessage);
            }
            case "last" -> {
                String last = account.getContext().getFake().getLastNick();
                if (last == null || last.isBlank()) {
                    player.sendMessage(MenuTexts.legacy(player, "menus.fake.no-last"));
                    return;
                }
                FakeService.enable(account, player, last,
                        msg -> {
                            player.sendMessage(msg);
                            openMainMenu(player);
                        },
                        player::sendMessage);
            }
            case "off" -> FakeService.disable(account, player, msg -> {
                player.sendMessage(msg);
                openMainMenu(player);
            });
            case "help" -> player.sendMessage(MenuTexts.legacy(player, "menus.fake.help-chat"));
        }
    }

    private static void fillGlass(Inventory inv) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.displayName(Component.empty());
        glass.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
    }

    private static ItemStack labeled(Player player, Material material, String legacyName, List<String> legacyLore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(ModernText.parse(legacyName));
        if (legacyLore != null && !legacyLore.isEmpty()) {
            meta.lore(legacyLore.stream().map(ModernText::parse).toList());
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack actionItem(Player player, Material material, String legacyName, List<String> legacyLore, String action) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(ModernText.parse(legacyName));
        if (legacyLore != null && !legacyLore.isEmpty()) {
            meta.lore(legacyLore.stream().map(ModernText::parse).toList());
        }
        meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, action);
        stack.setItemMeta(meta);
        return stack;
    }

    private static String getAction(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(ACTION_KEY, PersistentDataType.STRING);
    }
}
