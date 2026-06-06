package com.minecraft.lobbyhost.message;

import com.minecraft.lobbyhost.util.ModernText;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;

/** Atalhos para textos de menus com idioma do jogador. */
public final class MenuTexts {

    private MenuTexts() {}

    public static PlayerLanguage lang(Player player) {
        return LanguageService.of(player);
    }

    public static String raw(Player player, String key) {
        return Messages.raw(key, lang(player));
    }

    public static String format(Player player, String key, Map<String, String> placeholders) {
        return Messages.format(key, lang(player), placeholders);
    }

    public static String mini(Player player, String key) {
        return Messages.raw(key, lang(player));
    }

    public static String mini(Player player, String key, Map<String, String> placeholders) {
        return Messages.format(key, lang(player), placeholders);
    }

    public static Component component(Player player, String key) {
        return ModernText.parse(Messages.raw(key, lang(player)));
    }

    public static Component component(Player player, String key, Map<String, String> placeholders) {
        return ModernText.parse(Messages.format(key, lang(player), placeholders));
    }

    public static Component title(Player player, String key) {
        return ModernText.noItalic(component(player, key));
    }

    public static Map<String, String> map(String k1, String v1) {
        return Map.of(k1, v1 != null ? v1 : "");
    }

    public static Map<String, String> map(String k1, String v1, String k2, String v2) {
        return Map.of(k1, v1 != null ? v1 : "", k2, v2 != null ? v2 : "");
    }
}
