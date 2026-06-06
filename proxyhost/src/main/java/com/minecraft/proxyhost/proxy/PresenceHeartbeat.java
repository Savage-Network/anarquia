package com.minecraft.proxyhost.proxy;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.backend.data.NetworkPresenceData;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.concurrent.TimeUnit;

/** Renova last_seen de jogadores conectados no proxy. */
public final class PresenceHeartbeat {

    private static final long INTERVAL_SECONDS = 15;

    private PresenceHeartbeat() {}

    public static void start(ProxyServer proxy) {
        proxy.getScheduler()
                .buildTask(Core.getPlugin(), () -> tick(proxy))
                .repeat(INTERVAL_SECONDS, TimeUnit.SECONDS)
                .schedule();
    }

    private static void tick(ProxyServer proxy) {
        NetworkPresenceData presence = Core.getNetworkPresenceData();
        if (presence == null) return;

        for (var player : proxy.getAllPlayers()) {
            presence.touch(player.getUniqueId());
        }
        presence.pruneStale();
    }
}
