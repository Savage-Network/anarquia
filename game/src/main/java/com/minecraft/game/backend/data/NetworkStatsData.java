package com.minecraft.game.backend.data;

import com.minecraft.game.Core;
import com.minecraft.game.backend.database.HikariProvider;
import com.minecraft.game.backend.model.NetworkStatsEntry;
import com.minecraft.game.leaderboard.LeaderboardType;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Leitura e escrita em {@code network_stats} (lobby + game). */
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
        upsert(NetworkStatsEntry.builder()
                .statKey("player:" + uuid)
                .category(NetworkStatsEntry.Category.PLAYER)
                .displayName(trim(name))
                .kills(kills)
                .deaths(deaths)
                .golds(golds)
                .clanPower(0)
                .clanMembers(0)
                .bestKillStreak(bestKillStreak)
                .build());
    }

    public void upsertClan(String uuid, String name, int power, int members) {
        upsert(NetworkStatsEntry.builder()
                .statKey("clan:" + uuid)
                .category(NetworkStatsEntry.Category.CLAN)
                .displayName(trim(name))
                .kills(0)
                .deaths(0)
                .golds(0)
                .clanPower(power)
                .clanMembers(members)
                .bestKillStreak(0)
                .build());
    }

    public void upsert(NetworkStatsEntry e) {
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
            bind(ps, e);
            ps.executeUpdate();
        } catch (SQLException ex) {
            Core.getLogger().severe("Erro ao upsert network_stats: " + ex.getMessage());
        }
    }

    public List<NetworkStatsEntry> top(LeaderboardType type, int limit) {
        String sql = switch (type) {
            case KILLS -> playerOrder("kills DESC");
            case DEATHS -> playerOrder("deaths DESC");
            case GOLDS -> playerOrder("golds DESC");
            case KDR -> playerOrder(kdrOrderExpr());
            case CLANS -> "SELECT * FROM network_stats WHERE category = 'CLAN' ORDER BY clan_power DESC LIMIT ?";
        };
        List<NetworkStatsEntry> out = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao ler top " + type + ": " + e.getMessage());
        }
        return out;
    }

    public Optional<NetworkStatsEntry> find(String statKey) {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM network_stats WHERE stat_key = ?")) {
            ps.setString(1, statKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    private static String playerOrder(String orderExpr) {
        return "SELECT * FROM network_stats WHERE category = 'PLAYER' ORDER BY " + orderExpr + " LIMIT ?";
    }

    private String kdrOrderExpr() {
        String ratio = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? "CAST(kills AS DECIMAL(12,4)) / deaths"
                : "CAST(kills AS REAL) / deaths";
        return "(CASE WHEN deaths = 0 THEN kills ELSE " + ratio + " END) DESC";
    }

    private static void bind(PreparedStatement ps, NetworkStatsEntry e) throws SQLException {
        ps.setString(1, e.getStatKey());
        ps.setString(2, e.getCategory().name());
        ps.setString(3, e.getDisplayName());
        ps.setInt(4, e.getKills());
        ps.setInt(5, e.getDeaths());
        ps.setInt(6, e.getGolds());
        ps.setInt(7, e.getClanPower());
        ps.setInt(8, e.getClanMembers());
        ps.setInt(9, e.getBestKillStreak());
        ps.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
    }

    private static NetworkStatsEntry map(ResultSet rs) throws SQLException {
        return NetworkStatsEntry.builder()
                .statKey(rs.getString("stat_key"))
                .category(NetworkStatsEntry.Category.valueOf(rs.getString("category")))
                .displayName(rs.getString("display_name"))
                .kills(rs.getInt("kills"))
                .deaths(rs.getInt("deaths"))
                .golds(rs.getInt("golds"))
                .clanPower(rs.getInt("clan_power"))
                .clanMembers(rs.getInt("clan_members"))
                .bestKillStreak(rs.getInt("best_kill_streak"))
                .build();
    }

    private static String trim(String s) {
        if (s == null) return "?";
        return s.length() <= 32 ? s : s.substring(0, 32);
    }
}
