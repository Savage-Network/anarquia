package com.minecraft.lobbyhost.network;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;

/** Publica alterações de acesso aos eventos para o proxy (Velocity). */
public final class EventosAccessService {

    private EventosAccessService() {}

    public static boolean publishToggle(boolean open) {
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) return false;
        Core.getRedis().publish(Constant.REDIS_PROXY_SETTINGS_CHANNEL, "eventos-access|" + (open ? "1" : "0"));
        return true;
    }

    public static String statusColored() {
        return EventosAccessGuard.isOpen() ? "§aABERTO" : "§cFECHADO";
    }
}
