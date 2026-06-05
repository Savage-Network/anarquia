package com.minecraft.auth.util;

import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Detecta conta original Mojang vs pirata pelo UUID (funciona com proxy offline-mode). */
public final class PlayerOriginDetector {

    private PlayerOriginDetector() {}

    public static boolean isMojangOriginal(Player player) {
        if (player == null) return false;
        UUID uuid = player.getUniqueId();
        if (uuid == null) return false;
        return !uuid.equals(offlineUuid(player.getName()));
    }

    public static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }
}
