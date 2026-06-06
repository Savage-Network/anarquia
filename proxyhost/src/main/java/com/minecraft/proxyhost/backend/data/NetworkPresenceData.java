package com.minecraft.proxyhost.backend.data;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Presenca online na rede — usada pelo bot Discord e API (nao depende do JSON accounts).
 */
@RequiredArgsConstructor
public final class NetworkPresenceData {

    public static final String TABLE = "network_presence";
    private static final int STALE_SECONDS = 90;

    private final HikariProvider database;

    public void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS network_presence (
                    player_id     VARCHAR(36)  NOT NULL PRIMARY KEY,
                    player_name   VARCHAR(16)  NOT NULL,
                    server_id     VARCHAR(32)  NOT NULL DEFAULT 'proxy',
                    connected_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    last_seen_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar tabela " + TABLE, e);
        }

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "CREATE INDEX IF NOT EXISTS idx_network_presence_seen ON network_presence(last_seen_at)")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public void upsert(UUID playerId, String playerName, String serverId) {
        if (playerId == null || playerName == null || playerName.isBlank()) return;
        String server = serverId == null || serverId.isBlank() ? "proxy" : serverId;

        String upsert = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                INSERT INTO network_presence (player_id, player_name, server_id, connected_at, last_seen_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    player_name = VALUES(player_name),
                    server_id = VALUES(server_id),
                    last_seen_at = CURRENT_TIMESTAMP
                """
                : """
                INSERT INTO network_presence (player_id, player_name, server_id, connected_at, last_seen_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON CONFLICT(player_id) DO UPDATE SET
                    player_name = excluded.player_name,
                    server_id = excluded.server_id,
                    last_seen_at = CURRENT_TIMESTAMP
                """;

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(upsert)) {
            ps.setString(1, playerId.toString());
            ps.setString(2, playerName);
            ps.setString(3, server);
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().warn("Falha ao atualizar presenca de {}: {}", playerName, e.getMessage());
        }
    }

    public void touch(UUID playerId) {
        if (playerId == null) return;
        String sql = "UPDATE network_presence SET last_seen_at = CURRENT_TIMESTAMP WHERE player_id = ?";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().debug("Falha ao renovar presenca de {}: {}", playerId, e.getMessage());
        }
    }

    public void remove(UUID playerId) {
        if (playerId == null) return;
        String sql = "DELETE FROM network_presence WHERE player_id = ?";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().warn("Falha ao remover presenca de {}: {}", playerId, e.getMessage());
        }
    }

    public void pruneStale() {
        String sql = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? "DELETE FROM network_presence WHERE last_seen_at < DATE_SUB(NOW(), INTERVAL ? SECOND)"
                : "DELETE FROM network_presence WHERE last_seen_at < datetime('now', '-' || ? || ' seconds')";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, STALE_SECONDS);
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().debug("Falha ao limpar presencas antigas: {}", e.getMessage());
        }
    }
}
