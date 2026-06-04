package com.minecraft.lobbyhost.backend.sync;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.api.clan.Clan;
import com.minecraft.lobbyhost.backend.data.NetworkStatsData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Importa contas e clãs existentes para {@code network_stats} (primeira carga / reparo).
 */
public final class StatsBackfillService {

    private StatsBackfillService() {}

    public static void runAsync() {
        if (Core.getNetworkStatsData() == null || Core.getDatabase() == null) return;
        Core.getJavaPlugin().getServer().getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            int players = backfillPlayers();
            int clans = backfillClans();
            Core.getLogger().info("[network_stats] Backfill: " + players + " jogadores, " + clans + " clãs.");
            com.minecraft.lobbyhost.leaderboard.LeaderboardCache.refreshAll();
        });
    }

    private static int backfillPlayers() {
        int count = 0;
        String sql = "SELECT id, name, data FROM accounts";
        try (Connection c = Core.getDatabase().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            NetworkStatsData data = Core.getNetworkStatsData();
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String json = rs.getString("data");
                int kills = 0, deaths = 0, golds = 0, best = 0;
                try {
                    JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                    if (root.has("golds")) golds = root.get("golds").getAsInt();
                    if (root.has("gameStats") && root.get("gameStats").isJsonObject()) {
                        JsonObject gs = root.getAsJsonObject("gameStats");
                        if (gs.has("kills")) kills = gs.get("kills").getAsInt();
                        if (gs.has("deaths")) deaths = gs.get("deaths").getAsInt();
                        if (gs.has("bestKillStreak")) best = gs.get("bestKillStreak").getAsInt();
                    }
                } catch (Exception ignored) {
                }
                data.upsertPlayer(id, name, kills, deaths, golds, best);
                count++;
            }
        } catch (Exception e) {
            Core.getLogger().severe("[network_stats] Backfill players: " + e.getMessage());
        }
        return count;
    }

    private static int backfillClans() {
        int count = 0;
        try (Connection c = Core.getDatabase().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, name, data FROM clans");
             ResultSet rs = ps.executeQuery()) {
            NetworkStatsData data = Core.getNetworkStatsData();
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String json = rs.getString("data");
                int power = 0, members = 0;
                try {
                    Clan clan = Core.GSON.fromJson(json, Clan.class);
                    if (clan != null) {
                        power = clan.getPower();
                        members = clan.getMembers() != null ? clan.getMembers().size() : 0;
                        name = clan.getName();
                    }
                } catch (Exception ignored) {
                }
                data.upsertClan(id, name, power, members);
                count++;
            }
        } catch (Exception e) {
            Core.getLogger().severe("[network_stats] Backfill clans: " + e.getMessage());
        }
        return count;
    }
}
