package com.minecraft.lobbyhost.message;

import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.util.ModernText;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.Map;

public final class Messages {

    private static FileConfiguration config;

    private Messages() {}

    public static void load(LobbyHostPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static void reload(LobbyHostPlugin plugin) {
        load(plugin);
    }

    public static String raw(String path, PlayerLanguage lang, String def) {
        if (config == null) return def != null ? def : path;
        String localized = config.getString(lang.id() + "." + path);
        if (localized != null) return localized;
        return config.getString("pt." + path, def != null ? def : path);
    }

    public static String raw(String path, PlayerLanguage lang) {
        return raw(path, lang, path);
    }

    public static String format(String path, PlayerLanguage lang) {
        return format(path, lang, Collections.emptyMap());
    }

    public static String format(String path, PlayerLanguage lang, Map<String, String> placeholders) {
        String msg = raw(path, lang, path);
        if (placeholders == null || placeholders.isEmpty()) return msg;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String value = entry.getValue() != null ? entry.getValue() : "";
            msg = msg.replace("{" + entry.getKey() + "}", value);
        }
        return msg;
    }

    public static Component component(String path, PlayerLanguage lang) {
        return ModernText.parse(format(path, lang));
    }

    public static Component component(String path, PlayerLanguage lang, Map<String, String> placeholders) {
        return ModernText.parse(format(path, lang, placeholders));
    }

    public static Component component(String path, CommandSender sender) {
        return component(path, LanguageService.of(sender));
    }

    public static Component component(String path, Player player) {
        return component(path, LanguageService.of(player));
    }

    public static Component component(String path, Player player, Map<String, String> placeholders) {
        return component(path, LanguageService.of(player), placeholders);
    }

    public static Component prefixed(String path, PlayerLanguage lang) {
        return ModernText.parse(raw("prefix", lang, "") + format(path, lang));
    }

    public static Component prefixed(String path, Player player) {
        return prefixed(path, LanguageService.of(player));
    }

    public static Component component(String path) {
        return component(path, PlayerLanguage.PT);
    }

    public static Component component(String path, Map<String, String> placeholders) {
        return component(path, PlayerLanguage.PT, placeholders);
    }

    public static String raw(String path) {
        return raw(path, PlayerLanguage.PT);
    }
}
