package com.minecraft.game.message;

import com.minecraft.game.account.Account;
import com.minecraft.game.util.ModernText;
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

    public static String raw(Player player, String key, String fallback) {
        return Messages.raw(key, lang(player), fallback);
    }

    public static String format(Player player, String key, Map<String, String> placeholders) {
        return Messages.format(key, lang(player), placeholders);
    }

    public static String legacy(Player player, String key) {
        return Messages.legacy(key, lang(player));
    }

    public static String legacy(Player player, String key, Map<String, String> placeholders) {
        return Messages.legacy(key, lang(player), placeholders);
    }

    /** Legacy com fallback quando a chave ainda nao existe no YAML. */
    public static String legacyOr(Player player, String key, String fallback) {
        return Messages.legacy(key, lang(player), Collections.emptyMap());
    }

    public static Component component(Player player, String key) {
        return ModernText.parse(Messages.raw(key, lang(player)));
    }

    public static Component component(Player player, String key, Map<String, String> placeholders) {
        return ModernText.parse(Messages.format(key, lang(player), placeholders));
    }

    public static String title(Player player, String key) {
        return legacy(player, key);
    }

    public static Map<String, String> map(String k1, String v1) {
        return Map.of(k1, v1 != null ? v1 : "");
    }

    public static Map<String, String> map(String k1, String v1, String k2, String v2) {
        return Map.of(k1, v1 != null ? v1 : "", k2, v2 != null ? v2 : "");
    }
}
