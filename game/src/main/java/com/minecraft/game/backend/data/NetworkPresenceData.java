package com.minecraft.game.backend.data;

import com.minecraft.game.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/** Leitura de {@code network_presence} (escrita feita pelo proxy). */
@RequiredArgsConstructor
public final class NetworkPresenceData {

    public static final String TABLE = "network_presence";
    private static final int STALE_SECONDS = 90;

    private final HikariProvider database;

    public record Presence(UUID playerId, String playerName, String serverId) {}

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
}
