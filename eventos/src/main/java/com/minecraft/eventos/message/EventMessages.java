package com.minecraft.eventos.message;

import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.util.ModernText;
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

/** Mensagens carregadas de {@code messages.yml}. */
public final class EventMessages {

    private static FileConfiguration config;

    private EventMessages() {}

    public static void load(EventosPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        mergeDefaults(plugin, file);
    }

    public static void reload(EventosPlugin plugin) {
        load(plugin);
    }

    private static void mergeDefaults(EventosPlugin plugin, File file) {
        try (InputStream in = plugin.getResource("messages.yml")) {
            if (in == null) return;
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            config.setDefaults(defaults);
            config.options().copyDefaults(true);
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("[EventMessages] merge: " + e.getMessage());
        }
    }

    public static String raw(String path, String def) {
        if (config == null) return def != null ? def : path;
        String pt = config.getString("pt." + path);
        if (pt != null) return pt;
        return config.getString(path, def != null ? def : path);
    }

    public static String format(String path, Map<String, String> placeholders, String def) {
        String msg = raw(path, def);
        if (placeholders == null || placeholders.isEmpty()) return msg;
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            msg = msg.replace("{" + e.getKey() + "}", e.getValue() != null ? e.getValue() : "");
        }
        return msg;
    }

    public static String format(String path, Map<String, String> placeholders) {
        return format(path, placeholders, path);
    }

    public static String format(String path) {
        return format(path, Collections.emptyMap(), path);
    }

    public static Component component(String path) {
        return ModernText.parse(format(path));
    }

    public static Component component(String path, Map<String, String> placeholders) {
        return ModernText.parse(format(path, placeholders));
    }

    public static void send(CommandSender sender, String path) {
        sender.sendMessage(component(path));
    }

    public static void send(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(component(path, placeholders));
    }

    public static void sendPrefixed(Player player, String path, Map<String, String> placeholders) {
        player.sendMessage(ModernText.parse(
                ModernText.header() + " " + ModernText.mini(format(path, placeholders, path))));
    }
}
