package com.minecraft.game.util;

import com.minecraft.game.Core;
import com.minecraft.game.config.DiscordConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** POST simples para webhooks Discord (embeds JSON). */
public final class DiscordWebhookUtil {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private DiscordWebhookUtil() {}

    public static void send(String webhookKey, String content) {
        send(webhookKey, content, null);
    }

    public static void send(String webhookKey, String content, String embedTitle) {
        String url = DiscordConfig.webhook(webhookKey);
        if (url == null || url.isBlank()) return;

        String json = embedTitle != null && !embedTitle.isBlank()
                ? """
                {"content":%s,"embeds":[{"title":%s,"description":%s,"color":16744272}]}
                """.formatted(jsonString(content), jsonString(embedTitle), jsonString(content))
                : """
                {"content":%s}
                """.formatted(jsonString(content));

        Core.getJavaPlugin().getServer().getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(8))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                HTTP.send(req, HttpResponse.BodyHandlers.discarding());
            } catch (Exception e) {
                Core.getLogger().warning("[Discord] Webhook falhou (" + webhookKey + "): " + e.getMessage());
            }
        });
    }

    private static String jsonString(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "") + "\"";
    }
}
