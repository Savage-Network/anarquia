package com.minecraft.game.message;

import com.minecraft.game.GamePlugin;
import com.minecraft.game.util.ModernText;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/** Mensagens do plugin carregadas de {@code messages.yml} (pt/en). */
public final class Messages {

    private static FileConfiguration config;

    private Messages() {}

    public static void load(GamePlugin plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        mergeDefaults(plugin, file);
    }

    private static void mergeDefaults(GamePlugin plugin, File file) {
        try (InputStream in = plugin.getResource("messages.yml")) {
            if (in == null) return;
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            config.setDefaults(defaults);
            config.options().copyDefaults(true);
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("[Messages] Falha ao mesclar defaults: " + e.getMessage());
        }
    }

    public static void reload(GamePlugin plugin) {
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

    public static String format(String path, PlayerLanguage lang, Map<String, String> placeholders) {
        String msg = raw(path, lang, path);
        if (placeholders == null || placeholders.isEmpty()) return msg;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String value = entry.getValue() != null ? entry.getValue() : "";
            msg = msg.replace("{" + entry.getKey() + "}", value);
        }
        return msg;
    }

    public static String format(String path, PlayerLanguage lang) {
        return format(path, lang, Collections.emptyMap());
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

    public static Component component(String path, CommandSender sender, Map<String, String> placeholders) {
        return component(path, LanguageService.of(sender), placeholders);
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

    public static Component prefixed(String path, PlayerLanguage lang, Map<String, String> placeholders) {
        return ModernText.parse(raw("prefix", lang, "") + format(path, lang, placeholders));
    }

    public static Component prefixed(String path, Player player) {
        return prefixed(path, LanguageService.of(player));
    }

    public static String legacy(String path, PlayerLanguage lang) {
        return ModernText.legacy(format(path, lang));
    }

    public static String legacy(String path, PlayerLanguage lang, Map<String, String> placeholders) {
        return ModernText.legacy(format(path, lang, placeholders));
    }

    /** Compat: idioma padrao PT. */
    public static Component component(String path) {
        return component(path, PlayerLanguage.PT);
    }

    public static Component component(String path, Map<String, String> placeholders) {
        return component(path, PlayerLanguage.PT, placeholders);
    }

    public static Component prefixed(String path) {
        return prefixed(path, PlayerLanguage.PT);
    }

    public static Component prefixed(String path, Map<String, String> placeholders) {
        return prefixed(path, PlayerLanguage.PT, placeholders);
    }

    public static String raw(String path) {
        return raw(path, PlayerLanguage.PT);
    }
}
