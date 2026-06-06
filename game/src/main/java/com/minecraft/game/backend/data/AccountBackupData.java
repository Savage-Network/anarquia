package com.minecraft.game.backend.data;

import com.minecraft.game.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public final class AccountBackupData {

    public static final String TABLE = "account_backups";

    public record Snapshot(UUID playerId, String playerName, String backupType, String payload, long createdAt) {}

    private final HikariProvider database;

    public void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS account_backups (
                    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    player_id     VARCHAR(36)  NOT NULL,
                    player_name   VARCHAR(16)  NOT NULL,
                    backup_type   VARCHAR(32)  NOT NULL,
                    payload       LONGTEXT     NOT NULL,
                    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                )""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar " + TABLE, e);
        }
    }

    public void insert(UUID playerId, String playerName, String backupType, String payload) {
        String sql = "INSERT INTO account_backups (player_id, player_name, backup_type, payload) VALUES (?,?,?,?)";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            ps.setString(2, playerName);
            ps.setString(3, backupType);
            ps.setString(4, payload);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao gravar backup", e);
        }
    }

    public List<Snapshot> listRecent(int limit) {
        String sql = """
                SELECT player_id, player_name, backup_type, payload,
                       UNIX_TIMESTAMP(created_at) * 1000 AS ts
                FROM account_backups ORDER BY id DESC LIMIT ?
                """;
        List<Snapshot> out = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, Math.min(limit, 200)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Snapshot(
                            UUID.fromString(rs.getString("player_id")),
                            rs.getString("player_name"),
                            rs.getString("backup_type"),
                            rs.getString("payload"),
                            rs.getLong("ts")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar backups", e);
        }
        return out;
    }

    public int pruneOlderThanDays(int days) {
        String sql = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? "DELETE FROM account_backups WHERE created_at < DATE_SUB(NOW(), INTERVAL ? DAY)"
                : "DELETE FROM account_backups WHERE created_at < datetime('now', '-' || ? || ' days')";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, days);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao limpar backups", e);
        }
    }
}
