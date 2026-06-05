package com.minecraft.lobbyhost.network;

import com.minecraft.lobbyhost.Constant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class NetworkBroadcast {

    private static final LegacyComponentSerializer LEGACY_HEX = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private NetworkBroadcast() {}

    public static Component parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Component.empty();
        }
        int sep = raw.indexOf('|');
        String type = sep > 0 ? raw.substring(0, sep) : "ALERT";
        String payload = sep > 0 ? raw.substring(sep + 1) : raw;

        if ("BROADCAST".equalsIgnoreCase(type)) {
            return formatBroadcast(payload);
        }

        String[] parts = payload.split("\\|", 2);
        String body = parts.length >= 2 ? parts[1] : payload;
        return LEGACY.deserialize("§6§l" + Constant.SERVER_NAME + " §8» §f" + body.replace('&', '§'));
    }

    public static Component formatBroadcast(String message) {
        String body = message == null ? "" : message.replace('|', ' ').replace('&', '§');
        return LEGACY_HEX.deserialize("§x§0§6§4§0§2§B§lSAVAGE §b➤ §f" + body);
    }
}
