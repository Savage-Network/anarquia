package com.minecraft.proxyhost.routing;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.UuidUtils;

import java.util.UUID;

/**
 * Detecta se o jogador entrou com conta original Mojang (online-mode)
 * ou com UUID offline (launcher pirata / modo offline).
 */
public final class PlayerOriginDetector {

    private PlayerOriginDetector() {}

    public enum Origin {
        /** Conta comprada / autenticada na Mojang. */
        ORIGINAL,
        /** UUID offline ou proxy em modo offline. */
        CRACKED
    }

    public static Origin detect(Player player) {
        if (player == null) return Origin.CRACKED;
        if (!player.isOnlineMode()) {
            return Origin.CRACKED;
        }
        UUID uuid = player.getUniqueId();
        UUID offline = UuidUtils.generateOfflinePlayerUuid(player.getUsername());
        if (uuid != null && uuid.equals(offline)) {
            return Origin.CRACKED;
        }
        return Origin.ORIGINAL;
    }

    public static boolean isMojangOriginal(Player player) {
        return detect(player) == Origin.ORIGINAL;
    }
}
