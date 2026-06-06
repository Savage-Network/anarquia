package com.minecraft.lobbyhost.backend.data;

import com.minecraft.lobbyhost.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Leitura de {@code network_presence} (escrita pelo proxy Velocity). */
@RequiredArgsConstructor
public final class NetworkPresenceData {

    public static final String TABLE = "network_presence";
    private static final int STALE_SECONDS = 90;

    public record Presence(UUID playerId, String playerName, String serverId) {}

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
    }

    public Optional<Presence> findOnline(UUID playerId) {
        if (playerId == null) return Optional.empty();
        return findOnlineByIds(java.util.List.of(playerId)).values().stream().findFirst();
    }

    public Map<UUID, Presence> findOnlineByIds(Collection<UUID> playerIds) {
        Map<UUID, Presence> out = new HashMap<>();
        if (playerIds == null || playerIds.isEmpty()) return out;

        var ids = playerIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return out;

        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        String sql = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                SELECT player_id, player_name, server_id FROM network_presence
                WHERE player_id IN (%s)
                  AND last_seen_at >= DATE_SUB(NOW(), INTERVAL ? SECOND)
                """.formatted(placeholders)
                : """
                SELECT player_id, player_name, server_id FROM network_presence
                WHERE player_id IN (%s)
                  AND last_seen_at >= datetime('now', '-' || ? || ' seconds')
                """.formatted(placeholders);

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            for (UUID id : ids) {
                ps.setString(i++, id.toString());
            }
            ps.setInt(i, STALE_SECONDS);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("player_id"));
                    out.put(id, new Presence(
                            id,
                            rs.getString("player_name"),
                            rs.getString("server_id")));
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new RuntimeException("Falha ao buscar presenca na rede", e);
        }
        return out;
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
}
