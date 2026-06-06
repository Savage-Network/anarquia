package com.minecraft.lobbyhost.bukkit.menu.lang;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.message.LanguageService;
import com.minecraft.lobbyhost.message.Messages;
import com.minecraft.lobbyhost.message.PlayerLanguage;
import com.minecraft.lobbyhost.util.ModernText;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class LangMenus {

    private LangMenus() {}

    public static void open(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) {
            player.sendMessage(Messages.component("errors.account-not-loaded", player));
            return;
        }

        PlayerLanguage current = LanguageService.of(account);
        LangMenuHolder holder = new LangMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 27,
                ModernText.parse(Messages.raw("lang.menu-title", current)));
        holder.bind(inv);

        fillGlass(inv);
        inv.setItem(4, infoItem(current));
        inv.setItem(11, languageItem(PlayerLanguage.PT, current));
        inv.setItem(15, languageItem(PlayerLanguage.EN, current));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
    }

    public static void handleClick(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(LangKeys.LANG_ID, LangKeys.LANG_TYPE)) {
            return;
        }

        String langId = meta.getPersistentDataContainer().get(LangKeys.LANG_ID, LangKeys.LANG_TYPE);
        PlayerLanguage selected = PlayerLanguage.fromId(langId);
        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) {
            player.sendMessage(Messages.component("errors.account-not-loaded", player));
            return;
        }

        if (LanguageService.of(account) == selected) {
            player.closeInventory();
            return;
        }

        LanguageService.setLanguage(account, selected);
        player.sendMessage(Messages.component(
                selected == PlayerLanguage.PT ? "lang.changed-pt" : "lang.changed-en", selected));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
        open(player);
    }

    private static ItemStack infoItem(PlayerLanguage lang) {
        ItemStack stack = new ItemStack(Material.BOOK);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;
        meta.displayName(ModernText.parse(Messages.raw("lang.menu-title", lang))
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        meta.lore(List.of(
                ModernText.parse(Messages.raw("lang.menu-subtitle", lang))
                        .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false)));
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack languageItem(PlayerLanguage option, PlayerLanguage current) {
        boolean selected = option == current;
        Material material = option == PlayerLanguage.PT ? Material.GREEN_BANNER : Material.LIGHT_BLUE_BANNER;
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        String nameKey = option == PlayerLanguage.PT ? "lang.option-pt-name" : "lang.option-en-name";
        String loreKey = option == PlayerLanguage.PT ? "lang.option-pt-lore-1" : "lang.option-en-lore-1";
        meta.displayName(ModernText.parse(Messages.raw(nameKey, current))
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(ModernText.parse(Messages.raw(loreKey, current))
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(ModernText.parse(Messages.raw(selected ? "lang.selected" : "lang.click-select", current))
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(LangKeys.LANG_ID, LangKeys.LANG_TYPE, option.id());
        stack.setItemMeta(meta);
        return stack;
    }

    private static void fillGlass(Inventory inv) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.space());
            glass.setItemMeta(meta);
        }
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }
    }
}
