package com.minecraft.game.backend.data;

import com.minecraft.game.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Leitura de {@code network_presence} (escrita feita pelo proxy). */
@RequiredArgsConstructor
public final class NetworkPresenceData {

    public static final String TABLE = "network_presence";
    private static final int STALE_SECONDS = 90;

    public record Presence(UUID playerId, String playerName, String serverId) {}

    private final HikariProvider database;

    public void createTables() {
        String sql = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                CREATE TABLE IF NOT EXISTS network_presence (
                    player_id     VARCHAR(36)  NOT NULL PRIMARY KEY,
                    player_name   VARCHAR(16)  NOT NULL,
                    server_id     VARCHAR(32)  NOT NULL DEFAULT 'proxy',
                    connected_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    last_seen_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )"""
                : """
                CREATE TABLE IF NOT EXISTS network_presence (
                    player_id     TEXT NOT NULL PRIMARY KEY,
                    player_name   TEXT NOT NULL,
                    server_id     TEXT NOT NULL DEFAULT 'proxy',
                    connected_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_seen_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar tabela " + TABLE, e);
        }
    }

    public Optional<Presence> findOnline(UUID playerId) {
        if (playerId == null) return Optional.empty();
        String sql = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                SELECT player_id, player_name, server_id FROM network_presence
                WHERE player_id = ?
                  AND last_seen_at >= DATE_SUB(NOW(), INTERVAL ? SECOND)
                LIMIT 1
                """
                : """
                SELECT player_id, player_name, server_id FROM network_presence
                WHERE player_id = ?
                  AND last_seen_at >= datetime('now', '-' || ? || ' seconds')
                LIMIT 1
                """;
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            ps.setInt(2, STALE_SECONDS);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Presence(
                            UUID.fromString(rs.getString("player_id")),
                            rs.getString("player_name"),
                            rs.getString("server_id")));
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new RuntimeException("Falha ao buscar presenca", e);
        }
        return Optional.empty();
    }

    public Optional<Presence> findOnlineByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        String sql = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                SELECT player_id, player_name, server_id FROM network_presence
                WHERE LOWER(player_name) = LOWER(?)
                  AND last_seen_at >= DATE_SUB(NOW(), INTERVAL ? SECOND)
                LIMIT 1
                """
                : """
                SELECT player_id, player_name, server_id FROM network_presence
                WHERE LOWER(player_name) = LOWER(?)
                  AND last_seen_at >= datetime('now', '-' || ? || ' seconds')
                LIMIT 1
                """;
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setInt(2, STALE_SECONDS);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Presence(
                            UUID.fromString(rs.getString("player_id")),
                            rs.getString("player_name"),
                            rs.getString("server_id")));
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new RuntimeException("Falha ao buscar presenca de " + name, e);
        }
        return Optional.empty();
    }

    public Map<String, Integer> countByServer() {
        String sql = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                SELECT server_id, COUNT(*) AS c FROM network_presence
                WHERE last_seen_at >= DATE_SUB(NOW(), INTERVAL ? SECOND)
                GROUP BY server_id
                """
                : """
                SELECT server_id, COUNT(*) AS c FROM network_presence
                WHERE last_seen_at >= datetime('now', '-' || ? || ' seconds')
                GROUP BY server_id
                """;
        Map<String, Integer> out = new HashMap<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, STALE_SECONDS);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(rs.getString("server_id"), rs.getInt("c"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao contar presenca", e);
        }
        return out;
    }

    public int countTotal() {
        String sql = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? "SELECT COUNT(*) FROM network_presence WHERE last_seen_at >= DATE_SUB(NOW(), INTERVAL ? SECOND)"
                : "SELECT COUNT(*) FROM network_presence WHERE last_seen_at >= datetime('now', '-' || ? || ' seconds')";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, STALE_SECONDS);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao contar presenca total", e);
        }
        return 0;
    }
}
