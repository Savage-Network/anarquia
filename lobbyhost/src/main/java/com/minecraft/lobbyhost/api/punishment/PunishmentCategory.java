package com.minecraft.lobbyhost.api.punishment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PunishmentCategory {

    BAN("Banimento"),
    MUTE("Mute"),
    WARN("Advertência");

    private final String displayName;

    public static PunishmentCategory of(String raw) {
        if (raw == null) return null;
        return Arrays.stream(values())
                .filter(c -> c.name().equalsIgnoreCase(raw) || c.displayName.equalsIgnoreCase(raw))
                .findFirst()
                .orElse(null);
    }
}
