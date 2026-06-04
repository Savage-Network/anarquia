package com.minecraft.lobbyhost.leaderboard;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.backend.model.NetworkStatsEntry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Cache em memoria dos tops lidos de {@code network_stats}. */
public final class LeaderboardCache {

    private static final int TOP_LIMIT = 10;
    private static final Map<LeaderboardType, List<NetworkStatsEntry>> CACHE = new EnumMap<>(LeaderboardType.class);

    private LeaderboardCache() {}

    public static void refreshAll() {
        if (Core.getNetworkStatsData() == null) return;
        for (LeaderboardType type : LeaderboardType.values()) {
            CACHE.put(type, Core.getNetworkStatsData().top(type, TOP_LIMIT));
        }
    }

    public static List<NetworkStatsEntry> get(LeaderboardType type) {
        return CACHE.getOrDefault(type, List.of());
    }
}
