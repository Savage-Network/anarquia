package com.minecraft.proxyhost.proxy;

import com.minecraft.proxyhost.Constant;
import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.context.objects.rank.RankType;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.concurrent.TimeUnit;

/**
 * Publica online por servidor e estado de manutencao no Redis (TTL 30s).
 */
public final class NetworkPublisher {

    private static final int TTL_SECONDS = 30;
    private static final long INTERVAL_SECONDS = 10;

    private NetworkPublisher() {}

    public static void start(com.velocitypowered.api.proxy.ProxyServer proxy) {
        proxy.getScheduler()
                .buildTask(Core.getPlugin(), () -> tick(proxy))
                .repeat(INTERVAL_SECONDS, TimeUnit.SECONDS)
                .schedule();
    }

    private static void tick(com.velocitypowered.api.proxy.ProxyServer proxy) {
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) return;

        for (RegisteredServer server : proxy.getAllServers()) {
            String name = server.getServerInfo().getName();
            int online = server.getPlayersConnected().size();
            Core.getRedis().setEx(
                    Constant.REDIS_NETWORK_ONLINE_KEY_PREFIX + name,
                    String.valueOf(online),
                    TTL_SECONDS);
        }

        Core.getRedis().setEx(
                Constant.REDIS_NETWORK_ONLINE_TOTAL_KEY,
                String.valueOf(proxy.getPlayerCount()),
                TTL_SECONDS);

        ProxySettingsManager settings = ProxySettingsManager.get();
        if (settings == null) return;

        boolean maint = settings.isMaintenanceEnabled();
        RankType min = settings.getMaintenanceMinRank();
        MaintenanceMode mode = settings.getMaintenanceMode();
        String statusMsg = (maint ? "1" : "0") + "|" + min.name() + "|" + mode.name()
                + "|" + (com.minecraft.proxyhost.routing.EventosAccessGuard.isOpen() ? "1" : "0");
        Core.getRedis().publish(Constant.REDIS_NETWORK_STATUS_CHANNEL, statusMsg);
    }

    public static void publishStatusNow() {
        ProxySettingsManager settings = ProxySettingsManager.get();
        if (settings == null || Core.getRedis() == null || !Core.getRedis().isEnabled()) return;
        boolean maint = settings.isMaintenanceEnabled();
        Core.getRedis().publish(Constant.REDIS_NETWORK_STATUS_CHANNEL,
                (maint ? "1" : "0") + "|" + settings.getMaintenanceMinRank().name()
                        + "|" + settings.getMaintenanceMode().name()
                        + "|" + (com.minecraft.proxyhost.routing.EventosAccessGuard.isOpen() ? "1" : "0"));
    }
}
