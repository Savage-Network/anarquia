package com.minecraft.game.backend.data;

import com.minecraft.game.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class ActivityFeedData {

    public static final String TABLE = "activity_feed";

    public record Entry(long id, String type, String actorName, String message, String serverId, long createdAt) {}

    private final HikariProvider database;

    public void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS activity_feed (
                    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    type         VARCHAR(32)  NOT NULL,
                    actor_name   VARCHAR(16)  NOT NULL,
                    message      VARCHAR(255) NOT NULL,
                    server_id    VARCHAR(32)  NOT NULL DEFAULT 'network',
                    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                )""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar " + TABLE, e);
        }
    }

    public long insert(String type, String actorName, String message, String serverId) {
        String sql = "INSERT INTO activity_feed (type, actor_name, message, server_id) VALUES (?,?,?,?)";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type);
            ps.setString(2, actorName);
            ps.setString(3, message);
            ps.setString(4, serverId != null ? serverId : "network");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao inserir feed", e);
        }
        return -1;
    }

    public List<Entry> recent(int limit) {
        String sql = "SELECT id, type, actor_name, message, server_id, "
                + "UNIX_TIMESTAMP(created_at) * 1000 AS ts FROM activity_feed ORDER BY id DESC LIMIT ?";
        List<Entry> out = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, Math.min(limit, 100)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Entry(
                            rs.getLong("id"),
                            rs.getString("type"),
                            rs.getString("actor_name"),
                            rs.getString("message"),
                            rs.getString("server_id"),
                            rs.getLong("ts")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar feed", e);
        }
        return out;
    }
}
