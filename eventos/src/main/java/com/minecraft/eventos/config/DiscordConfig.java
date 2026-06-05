package com.minecraft.eventos.config;

import com.minecraft.eventos.EventosPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Discord SavageMC — {@code plugins/eventos/discord.yml}. */
public final class DiscordConfig {

    private static File file;
    private static FileConfiguration yaml;

    private static boolean enabled = true;
    private static String inviteDisplay = "discord.gg/savagemc";
    private static String inviteUrl = "https://discord.gg/savagemc";
    private static String guildId = "";
    private static String serverName = "SavageMC Discord";

    private DiscordConfig() {}

    public static void load(EventosPlugin plugin) {
        file = new File(plugin.getDataFolder(), "discord.yml");
        if (!file.exists()) {
            plugin.saveResource("discord.yml", false);
        }
        yaml = YamlConfiguration.loadConfiguration(file);
        mergeDefaults(plugin);
        readAll();
        plugin.getLogger().info("[Discord] " + inviteDisplay);
    }

    private static void mergeDefaults(EventosPlugin plugin) {
        try (InputStream in = plugin.getResource("discord.yml")) {
            if (in == null) return;
            yaml.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8)));
            yaml.options().copyDefaults(true);
            yaml.save(file);
        } catch (Exception ignored) {
        }
    }

    private static void readAll() {
        enabled = yaml.getBoolean("enabled", true);
        inviteDisplay = yaml.getString("invite.display", "discord.gg/savagemc");
        inviteUrl = yaml.getString("invite.url", "https://discord.gg/savagemc");
        guildId = yaml.getString("server.guild-id", "");
        serverName = yaml.getString("server.name", "SavageMC Discord");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static String inviteDisplay() {
        return inviteDisplay;
    }

    public static String inviteUrl() {
        return inviteUrl;
    }

    public static String guildId() {
        return guildId;
    }

    public static String serverName() {
        return serverName;
    }

    public static String webhook(String key) {
        if (yaml == null || key == null || key.isBlank()) return "";
        String value = yaml.getString("webhooks." + key, "");
        return value == null ? "" : value.trim();
    }

    public static boolean feature(String key) {
        if (yaml == null || key == null) return false;
        return yaml.getBoolean("features." + key, false);
    }

    public static String message(String key) {
        if (yaml == null || key == null) return "";
        String value = yaml.getString("messages." + key, "");
        return value == null ? "" : value;
    }

    public static List<String> access() {
        if (yaml == null) return List.of();
        return yaml.getStringList("access");
    }
}
