package com.minecraft.auth.message;

import com.minecraft.auth.AuthPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

/** Mensagens do auth carregadas de messages.yml. */
public final class AuthMessages {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static FileConfiguration config;

    private AuthMessages() {}

    public static void load(AuthPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static void reload(AuthPlugin plugin) {
        load(plugin);
    }

    public static String raw(String path, String def) {
        if (config == null) return colorize(def);
        return colorize(config.getString(path, def));
    }

    public static String raw(String path) {
        return raw(path, path);
    }

    public static Component component(String path, String def) {
        return LEGACY.deserialize(raw(path, def));
    }

    public static Component component(String path) {
        return component(path, path);
    }

    public static String format(String path, Map<String, String> placeholders) {
        String msg = raw(path, path);
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                msg = msg.replace("{" + e.getKey() + "}", e.getValue());
            }
        }
        return msg;
    }

    public static Component formatComponent(String path, Map<String, String> placeholders) {
        return LEGACY.deserialize(format(path, placeholders));
    }

    private static String colorize(String text) {
        if (text == null) return "";
        return text.replace('&', '§');
    }
}
