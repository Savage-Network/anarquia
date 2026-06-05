package com.minecraft.proxyhost.whitelist;

import java.util.Locale;

/** Alvos da whitelist da rede SavageMC. */
public enum WhitelistTarget {

    PROXY("Proxy", null),
    LOBBY("Lobby", "lobby"),
    GAME("Semi Anarquia", "game"),
    EVENTOS("Eventos", "eventos");

    private final String displayName;
    private final String velocityServer;

    WhitelistTarget(String displayName, String velocityServer) {
        this.displayName = displayName;
        this.velocityServer = velocityServer;
    }

    public String displayName() {
        return displayName;
    }

    public String velocityServer() {
        return velocityServer;
    }

    public static WhitelistTarget parse(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String key = raw.trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "proxy", "rede", "network" -> PROXY;
            case "lobby", "hub" -> LOBBY;
            case "game", "semi-anarquia", "semi_anarquia", "anarquia" -> GAME;
            case "eventos", "events" -> EVENTOS;
            default -> null;
        };
    }

    public static WhitelistTarget fromVelocityServer(String serverName) {
        if (serverName == null || serverName.isBlank()) return null;
        String key = serverName.trim().toLowerCase(Locale.ROOT);
        for (WhitelistTarget target : values()) {
            if (target.velocityServer != null && target.velocityServer.equalsIgnoreCase(key)) {
                return target;
            }
        }
        return null;
    }
}
