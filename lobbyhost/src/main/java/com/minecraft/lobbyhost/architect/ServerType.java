package com.minecraft.lobbyhost.architect;

/**
 * Tipos de lobby do {@code lobbyhost}. Mapeia 1:1 para o ServerType.HUB_*
 * do Savage. Cada tipo carrega um {@link Architect} diferente.
 */
public enum ServerType {

    HUB,
    HUB_BEDWARS,
    HUB_DUELS,
    HUB_EGGWARS,
    HUB_SKYWARS,
    HUB_THE_BRIDGE,
    HUB_PVP;

    public static ServerType parse(String raw) {
        if (raw == null) return HUB;
        try {
            return valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return HUB;
        }
    }
}
