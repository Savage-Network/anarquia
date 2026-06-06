package com.minecraft.eventos.util;

import com.minecraft.eventos.config.DiscordConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** POST para webhooks Discord. */
public final class DiscordWebhookUtil {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private DiscordWebhookUtil() {}

    public static void send(String webhookKey, String content, String embedTitle) {
        String url = DiscordConfig.webhook(webhookKey);
        if (url == null || url.isBlank()) return;

        String json = embedTitle != null && !embedTitle.isBlank()
                ? """
                {"embeds":[{"title":%s,"description":%s,"color":16744272}]}
                """.formatted(jsonString(embedTitle), jsonString(content))
                : """
                {"content":%s}
                """.formatted(jsonString(content));

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HTTP.sendAsync(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
        }
    }

    private static String jsonString(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "") + "\"";
    }
}
