package com.minecraft.proxyhost.backend.data;

import com.minecraft.proxyhost.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Leitura minima de {@code player_auth} para roteamento no proxy.
 */
@RequiredArgsConstructor
public final class PlayerAuthData {

    public static final String TABLE = "player_auth";

    private final HikariProvider database;

    public void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS player_auth (
                    player_id           VARCHAR(36) PRIMARY KEY,
                    password_hash       VARCHAR(128) NOT NULL DEFAULT '',
                    last_password_hash  VARCHAR(128),
                    recent_hashes       TEXT NOT NULL DEFAULT '[]',
                    registered_at       TIMESTAMP,
                    last_login_at       TIMESTAMP,
                    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar tabela " + TABLE, e);
        }
    }

    public boolean isRegistered(UUID playerId) {
        String sql = "SELECT 1 FROM player_auth WHERE player_id = ? AND password_hash <> '' LIMIT 1";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
