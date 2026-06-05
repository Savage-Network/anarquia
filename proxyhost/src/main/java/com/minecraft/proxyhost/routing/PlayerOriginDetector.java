package com.minecraft.proxyhost.routing;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.UuidUtils;

import java.util.UUID;

/**
 * Detecta conta original Mojang vs pirata/offline pelo UUID.
 *
 * <p>Funciona com Velocity em {@code online-mode = false} (rede semi-anarquia):
 * contas originais mantêm UUID Mojang; piratas usam UUID offline derivado do nick.
 */
public final class PlayerOriginDetector {

    private PlayerOriginDetector() {}

    public enum Origin {
        /** Conta comprada / autenticada na Mojang. */
        ORIGINAL,
        /** UUID offline (launcher pirata). */
        CRACKED
    }

    public static Origin detect(Player player) {
        if (player == null) return Origin.CRACKED;
        UUID uuid = player.getUniqueId();
        if (uuid == null) return Origin.CRACKED;
        UUID offline = UuidUtils.generateOfflinePlayerUuid(player.getUsername());
        if (uuid.equals(offline)) {
            return Origin.CRACKED;
        }
        return Origin.ORIGINAL;
    }

    public static boolean isMojangOriginal(Player player) {
        return detect(player) == Origin.ORIGINAL;
    }
}
