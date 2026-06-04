package com.minecraft.proxyhost.api.punishment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PunishmentReason {

    CHEATING("Uso de Trapaças"),
    COMMUNITY("Diretrizes da Comunidade");

    private final String displayName;

    public static PunishmentReason of(String raw) {
        if (raw == null) return null;
        return Arrays.stream(values())
                .filter(r -> r.name().equalsIgnoreCase(raw) || r.displayName.equalsIgnoreCase(raw))
                .findFirst()
                .orElse(null);
    }
}
