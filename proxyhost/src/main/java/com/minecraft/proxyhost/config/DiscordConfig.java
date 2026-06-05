package com.minecraft.proxyhost.config;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Discord SavageMC — {@code plugins/proxyhost/discord.yml}. */
public final class DiscordConfig {

    private static DiscordConfig instance;

    private final Path file;

    private boolean enabled = true;
    private String inviteDisplay = "discord.gg/savagemc";
    private String inviteUrl = "https://discord.gg/savagemc";
    private String guildId = "";
    private String serverName = "SavageMC Discord";
    private Map<String, Object> webhooks = Map.of();
    private Map<String, Object> features = Map.of();
    private Map<String, Object> messages = Map.of();
    private List<String> access = List.of();

    private DiscordConfig(Path dataDirectory) {
        this.file = dataDirectory.resolve("discord.yml");
    }

    public static void load(Path dataDirectory, Logger logger) {
        instance = new DiscordConfig(dataDirectory);
        instance.read();
        logger.info("[Discord] {} | webhooks ativos: {}", instance.inviteDisplay, instance.countWebhooks());
    }

    public static DiscordConfig get() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    private void read() {
        try {
            if (!Files.exists(file.getParent())) {
                Files.createDirectories(file.getParent());
            }
            if (!Files.exists(file)) {
                try (InputStream in = getClass().getResourceAsStream("/discord.yml")) {
                    if (in != null) Files.copy(in, file);
                }
            }
            try (InputStream in = Files.newInputStream(file)) {
                Yaml yaml = new Yaml();
                Map<String, Object> root = yaml.load(in);
                if (root == null) return;

                enabled = bool(root.get("enabled"), true);
                Map<String, Object> invite = map(root.get("invite"));
                inviteDisplay = str(invite.get("display"), "discord.gg/savagemc");
                inviteUrl = str(invite.get("url"), "https://discord.gg/savagemc");

                Map<String, Object> server = map(root.get("server"));
                guildId = str(server.get("guild-id"), "");
                serverName = str(server.get("name"), "SavageMC Discord");

                webhooks = map(root.get("webhooks"));
                features = map(root.get("features"));
                messages = map(root.get("messages"));
                Object accessRaw = root.get("access");
                if (accessRaw instanceof List<?> list) {
                    access = list.stream().map(String::valueOf).toList();
                }
            }
        } catch (Exception e) {
            com.minecraft.proxyhost.Core.getLogger().warn("[Discord] Falha ao carregar discord.yml: " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String inviteDisplay() {
        return inviteDisplay;
    }

    public String inviteUrl() {
        return inviteUrl;
    }

    public String guildId() {
        return guildId;
    }

    public String serverName() {
        return serverName;
    }

    public String webhook(String key) {
        if (key == null || key.isBlank()) return "";
        Object value = webhooks.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    public boolean feature(String key) {
        if (key == null) return false;
        Object value = features.get(key);
        return value instanceof Boolean b && b;
    }

    public String message(String key) {
        if (key == null) return "";
        Object value = messages.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    public List<String> access() {
        return access;
    }

    private int countWebhooks() {
        int count = 0;
        for (Object value : webhooks.values()) {
            if (value != null && !String.valueOf(value).isBlank()) count++;
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object raw) {
        if (raw instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        return Map.of();
    }

    private static String str(Object raw, String fallback) {
        return raw == null ? fallback : String.valueOf(raw);
    }

    private static boolean bool(Object raw, boolean fallback) {
        return raw instanceof Boolean b ? b : fallback;
    }
}
