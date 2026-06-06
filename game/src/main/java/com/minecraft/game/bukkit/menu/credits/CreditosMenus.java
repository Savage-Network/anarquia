package com.minecraft.game.bukkit.menu.credits;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.message.LanguageService;
import com.minecraft.game.message.Messages;
import com.minecraft.game.message.PlayerLanguage;
import com.minecraft.game.util.ModernText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public final class CreditosMenus {

    /** Textura de globo (cabeca decorativa de mapa/mundo). */
    private static final String GLOBE_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zY2Y4YWE1ZTM0ZWI1Y2FmNjJhNjM3NmYyMDRjZmM4YmYwNzE4MWEzZDM4NTJkMzYyNTEzZjQ2MDg1MTUyIn19fQ==";

    private static final String DEV_NAME = "zrwfaWASD";
    private static final String PLANET_MINECRAFT_URL = "https://www.planetminecraft.com/";

    private CreditosMenus() {}

    public static void open(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());
        PlayerLanguage lang = account != null ? LanguageService.of(account) : LanguageService.of(player);

        CreditosMenuHolder holder = new CreditosMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 27,
                ModernText.parse(Messages.raw("credits.menu-title", lang)));
        holder.bind(inv);

        fillGlass(inv);
        inv.setItem(11, devHead(lang));
        inv.setItem(15, mapHead(lang));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.4f);
    }

    public static void handleClick(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String clickId = meta.getPersistentDataContainer().get(CreditosKeys.CLICK_ID, CreditosKeys.CLICK_TYPE);
        if (clickId == null) return;

        Account account = Core.getAccountController().of(player.getUniqueId());
        PlayerLanguage lang = account != null ? LanguageService.of(account) : LanguageService.of(player);

        if ("map".equals(clickId)) {
            Component link = ModernText.parse(Messages.raw("credits.map-link", lang))
                    .decoration(TextDecoration.ITALIC, false)
                    .clickEvent(ClickEvent.openUrl(PLANET_MINECRAFT_URL));
            player.sendMessage(link);
        }
    }

    private static ItemStack devHead(PlayerLanguage lang) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        if (meta == null) return stack;

        OfflinePlayer dev = Bukkit.getOfflinePlayer(DEV_NAME);
        meta.setOwningPlayer(dev);
        meta.displayName(ModernText.parse(Messages.raw("credits.dev-name", lang))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                ModernText.parse(Messages.raw("credits.dev-role", lang))
                        .decoration(TextDecoration.ITALIC, false),
                ModernText.parse(Messages.raw("credits.dev-desc", lang))
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                ModernText.parse(Messages.format("credits.dev-nick", lang, java.util.Map.of("nick", DEV_NAME)))
                        .decoration(TextDecoration.ITALIC, false)
        ));
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack mapHead(PlayerLanguage lang) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        if (meta == null) return stack;

        PlayerProfile profile = Bukkit.createProfile(
                UUID.nameUUIDFromBytes("savage-planetminecraft-map".getBytes()),
                "Mapa");
        profile.setProperty(new ProfileProperty("textures", GLOBE_TEXTURE));
        meta.setPlayerProfile(profile);
        meta.displayName(ModernText.parse(Messages.raw("credits.map-name", lang))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                ModernText.parse(Messages.raw("credits.map-role", lang))
                        .decoration(TextDecoration.ITALIC, false),
                ModernText.parse(Messages.raw("credits.map-desc", lang))
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                ModernText.parse(Messages.raw("credits.map-click", lang))
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(CreditosKeys.CLICK_ID, CreditosKeys.CLICK_TYPE, "map");
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
