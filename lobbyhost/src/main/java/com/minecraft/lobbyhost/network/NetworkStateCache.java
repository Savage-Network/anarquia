package com.minecraft.lobbyhost.network;

import com.minecraft.lobbyhost.account.context.objects.rank.RankType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Estado em cache da rede (online por modo, manutencao). */
public final class NetworkStateCache {

    private static volatile boolean maintenance;
    private static volatile RankType maintenanceMinRank = RankType.HELPER;
    private static final Map<String, Integer> ONLINE = new ConcurrentHashMap<>();

    private NetworkStateCache() {}

    public static boolean isMaintenance() {
        return maintenance;
    }

    public static void setMaintenance(boolean enabled, RankType minRank) {
        maintenance = enabled;
        if (minRank != null) maintenanceMinRank = minRank;
    }

    public static RankType maintenanceMinRank() {
        return maintenanceMinRank;
    }

    public static void setOnline(String heartbeatId, int count) {
        if (heartbeatId == null || heartbeatId.isBlank()) return;
        ONLINE.put(heartbeatId.toLowerCase(), Math.max(0, count));
    }

    public static int getOnline(String heartbeatId) {
        if (heartbeatId == null) return 0;
        return ONLINE.getOrDefault(heartbeatId.toLowerCase(), 0);
    }

    public static boolean canJoinDuringMaintenance(com.minecraft.lobbyhost.account.Account account) {
        if (!maintenance) return true;
        if (account == null) return false;
        if (account.isStaffer()) return true;
        return account.getRankType().isAtLeast(maintenanceMinRank);
    }
}
