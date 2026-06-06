package com.minecraft.game.bukkit.hologram;

import java.util.Arrays;
import java.util.Optional;

/** Tipos de holograma registrados no servidor. */
public enum HologramType {

    EXPLICACAO("explicacao", "hologram.explicacao");

    private final String id;
    private final String messageKey;

    HologramType(String id, String messageKey) {
        this.id = id;
        this.messageKey = messageKey;
    }

    public String id() {
        return id;
    }

    public String messageKey() {
        return messageKey;
    }

    public static Optional<HologramType> fromId(String raw) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        String key = raw.toLowerCase();
        return Arrays.stream(values())
                .filter(type -> type.id.equals(key))
                .findFirst();
    }
}
