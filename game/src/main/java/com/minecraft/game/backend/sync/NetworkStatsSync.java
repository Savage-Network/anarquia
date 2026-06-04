package com.minecraft.game.backend.sync;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.Clan;

public final class NetworkStatsSync {

    private NetworkStatsSync() {}

    public static void syncPlayer(Account account) {
        if (account == null || Core.getNetworkStatsData() == null) return;
        var stats = account.getGameStats();
        Core.getNetworkStatsData().upsertPlayer(
                account.getId().toString(),
                account.getName(),
                stats.getKills(),
                stats.getDeaths(),
                account.getGolds(),
                stats.getBestKillStreak());
        publishInvalidate();
    }

    public static void syncClan(Clan clan) {
        if (clan == null || Core.getNetworkStatsData() == null) return;
        Core.getNetworkStatsData().upsertClan(
                clan.getId().toString(),
                clan.getName(),
                clan.getPower(),
                clan.getMembers() != null ? clan.getMembers().size() : 0);
        publishInvalidate();
    }

    private static void publishInvalidate() {
        if (Core.getRedis() != null && Core.getRedis().isEnabled()) {
            Core.getRedis().publish(Constant.REDIS_STATS_UPDATE_CHANNEL, "refresh");
        }
    }
}
