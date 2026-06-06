package com.minecraft.lobbyhost.bukkit.menu;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.fake.FakeService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class FakeMenus {

    private static final NamespacedKey ACTION_KEY = new NamespacedKey(LobbyHostPlugin.getInstance(), "fake_action");

    private FakeMenus() {}

    public static void openMainMenu(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) return;

        FakeMenuHolder holder = new FakeMenuHolder("fake_hub");
        Inventory inv = Bukkit.createInventory(holder, 45,
                Component.text("Fake Nick").color(NamedTextColor.DARK_AQUA)
                        .decoration(TextDecoration.ITALIC, false));
        holder.bind(inv);
        fillGlass(inv);

        boolean active = account.isFake();
        inv.setItem(4, labeled(Material.NAME_TAG, "?b?lFake Nick",
                List.of(
                        active ? "?7Atual: ?f" + account.getNickname() : "?7Voce esta com sua identidade real",
                        "?7Real: ?8" + account.getRealName(),
                        "",
                        "?7Disfarce rank, medalha e cla",
                        "?7Skin automatica da Mojang"
                )));

        inv.setItem(20, actionItem(Material.LIME_DYE, "?a?lNick Aleatorio",
                List.of("?7Gera um nick unico", "?7e aplica a skin dele", "", "?eClique para sortear"),
                "random"));

        inv.setItem(22, actionItem(Material.WRITABLE_BOOK, "?e?lNick Personalizado",
                List.of("?7Use no chat:", "?f/fake <nick>", "", "?7Exemplo: ?f/fake DarkWolfBR"),
                "help"));

        if (account.getContext().getFake().getLastNick() != null
                && !account.getContext().getFake().getLastNick().isBlank()) {
            inv.setItem(24, actionItem(Material.CLOCK, "?6?lUltimo Nick",
                    List.of("?7Reutilizar: ?f" + account.getContext().getFake().getLastNick(), "", "?eClique para equipar"),
                    "last"));
        }

        if (active) {
            inv.setItem(40, actionItem(Material.BARRIER, "?c?lRemover Fake",
                    List.of("?7Volta ao nick e skin reais", "", "?eClique para desativar"),
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
                    player.sendMessage("?cNenhum nick anterior salvo.");
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
            case "help" -> player.sendMessage("?eUse ?f/fake <nick> ?epara escolher um nick personalizado.");
        }
    }

    private static void fillGlass(Inventory inv) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.displayName(Component.empty());
        glass.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);
    }

    private static ItemStack labeled(Material material, String name, List<String> loreLines) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
        if (loreLines != null && !loreLines.isEmpty()) {
            meta.lore(loreLines.stream()
                    .map(l -> Component.text(l).decoration(TextDecoration.ITALIC, false))
                    .toList());
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack actionItem(Material material, String name, List<String> lore, String action) {
        ItemStack item = labeled(material, name, lore);
        setAction(item, action);
        return item;
    }

    private static void setAction(ItemStack item, String action) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, action);
        item.setItemMeta(meta);
    }

    private static String getAction(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(ACTION_KEY, PersistentDataType.STRING);
    }
}
