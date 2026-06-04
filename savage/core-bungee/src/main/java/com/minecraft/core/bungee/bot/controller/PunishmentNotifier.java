package com.minecraft.core.bungee.bot.controller;

import com.google.gson.JsonObject;
import com.minecraft.core.Core;
import com.minecraft.core.api.punishment.Punishment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Envia punições ao bot Discord standalone (HTTP).
 * Configure SAVAGE_BOT_URL (ex.: http://127.0.0.1:4040) e opcionalmente SAVAGE_BOT_SECRET.
 */
public final class PunishmentNotifier {

    private static final String DEFAULT_URL = "http://127.0.0.1:4040";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private PunishmentNotifier() {
    }

    public static void notify(String action, Punishment punishment) {
        String baseUrl = System.getenv("SAVAGE_BOT_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = DEFAULT_URL;
        }

        try {
            JsonObject body = new JsonObject();
            body.addProperty("action", action);

            JsonObject p = new JsonObject();
            p.addProperty("id", punishment.getId());
            p.addProperty("category", punishment.getCategory().name());
            p.addProperty("reason", punishment.getReason().getName());
            p.addProperty("cause", punishment.getCause());
            p.addProperty("server", punishment.getServer().getName());
            p.addProperty("arcade", punishment.getArcade().name());
            p.addProperty("playerName", punishment.getPlayerName());
            p.addProperty("playerUuid", punishment.getPlayer().toString());
            p.addProperty("authorName", punishment.getAuthorName());
            p.addProperty("expiresAt", punishment.getExpiresAt());

            if (punishment.getRevocation() != null) {
                com.minecraft.core.account.Account revoker = com.minecraft.core.Core.getAccountData()
                        .of(punishment.getRevocation().getAuthor(), false);
                p.addProperty("revokerName", revoker != null ? revoker.getName() : "CONSOLE");
                p.addProperty("revokeReason", punishment.getRevocation().getReason());
            }

            body.add("punishment", p);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl.replaceAll("/$", "") + "/punishment"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8));

            String secret = System.getenv("SAVAGE_BOT_SECRET");
            if (secret != null && !secret.isBlank()) {
                builder.header("X-Bot-Secret", secret);
            }

            HTTP.sendAsync(builder.build(), HttpResponse.BodyHandlers.discarding())
                    .exceptionally(ex -> {
                        Core.getLogger().warning("[PunishmentNotifier] Falha ao notificar bot: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            Core.getLogger().warning("[PunishmentNotifier] " + e.getMessage());
        }
    }
}
