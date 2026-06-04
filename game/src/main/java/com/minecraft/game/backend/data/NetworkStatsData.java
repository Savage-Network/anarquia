package com.minecraft.game.backend.data;

import com.minecraft.game.Core;
import com.minecraft.game.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/** Escrita em {@code network_stats} (mesma tabela que o lobby lê). */
@RequiredArgsConstructor
public final class NetworkStatsData {

    public static final String TABLE = "network_stats";

    private final HikariProvider database;

    public void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS network_stats (
                    stat_key          VARCHAR(80) PRIMARY KEY,
                    category          VARCHAR(16) NOT NULL,
                    display_name      VARCHAR(32) NOT NULL,
                    kills             INT NOT NULL DEFAULT 0,
                    deaths            INT NOT NULL DEFAULT 0,
                    golds             INT NOT NULL DEFAULT 0,
                    clan_power        INT NOT NULL DEFAULT 0,
                    clan_members      INT NOT NULL DEFAULT 0,
                    best_kill_streak  INT NOT NULL DEFAULT 0,
                    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar tabela " + TABLE, e);
        }
    }

    public void upsertPlayer(String uuid, String name, int kills, int deaths, int golds, int bestKillStreak) {
        upsertRow("player:" + uuid, "PLAYER", trim(name), kills, deaths, golds, 0, 0, bestKillStreak);
    }

    public void upsertClan(String uuid, String name, int power, int members) {
        upsertRow("clan:" + uuid, "CLAN", trim(name), 0, 0, 0, power, members, 0);
    }

    private void upsertRow(String key, String category, String displayName,
                           int kills, int deaths, int golds, int clanPower, int clanMembers, int bestStreak) {
        String upsert = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                INSERT INTO network_stats (stat_key, category, display_name, kills, deaths, golds,
                    clan_power, clan_members, best_kill_streak, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE category=VALUES(category), display_name=VALUES(display_name),
                    kills=VALUES(kills), deaths=VALUES(deaths), golds=VALUES(golds),
                    clan_power=VALUES(clan_power), clan_members=VALUES(clan_members),
                    best_kill_streak=VALUES(best_kill_streak), updated_at=VALUES(updated_at)"""
                : """
                INSERT INTO network_stats (stat_key, category, display_name, kills, deaths, golds,
                    clan_power, clan_members, best_kill_streak, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(stat_key) DO UPDATE SET category=excluded.category,
                    display_name=excluded.display_name, kills=excluded.kills, deaths=excluded.deaths,
                    golds=excluded.golds, clan_power=excluded.clan_power,
                    clan_members=excluded.clan_members, best_kill_streak=excluded.best_kill_streak,
                    updated_at=excluded.updated_at""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(upsert)) {
            ps.setString(1, key);
            ps.setString(2, category);
            ps.setString(3, displayName);
            ps.setInt(4, kills);
            ps.setInt(5, deaths);
            ps.setInt(6, golds);
            ps.setInt(7, clanPower);
            ps.setInt(8, clanMembers);
            ps.setInt(9, bestStreak);
            ps.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao upsert network_stats: " + e.getMessage());
        }
    }

    private static String trim(String s) {
        if (s == null) return "?";
        return s.length() <= 32 ? s : s.substring(0, 32);
    }
}
