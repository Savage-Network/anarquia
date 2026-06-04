package com.minecraft.game.backend.data;

import com.minecraft.game.backend.database.HikariProvider;
import com.minecraft.game.backend.model.PlayerReport;
import com.minecraft.game.report.ReportReason;
import com.minecraft.game.report.ReportStatus;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class ReportData {

    public static final String TABLE = "player_reports";

    private final HikariProvider database;

    public void createTables() {
        String sql = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                CREATE TABLE IF NOT EXISTS player_reports (
                    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                    reporter_uuid   VARCHAR(36) NOT NULL,
                    reporter_name   VARCHAR(16) NOT NULL,
                    target_uuid     VARCHAR(36) NOT NULL,
                    target_name     VARCHAR(16) NOT NULL,
                    reason          VARCHAR(24) NOT NULL,
                    details         TEXT,
                    server_id       VARCHAR(32) NOT NULL,
                    status          VARCHAR(16) NOT NULL DEFAULT 'OPEN',
                    staff_uuid      VARCHAR(36),
                    staff_note      TEXT,
                    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_status_created (status, created_at),
                    INDEX idx_target_created (target_uuid, created_at)
                )"""
                : """
                CREATE TABLE IF NOT EXISTS player_reports (
                    id              INTEGER PRIMARY KEY AUTOINCREMENT,
                    reporter_uuid   TEXT NOT NULL,
                    reporter_name   TEXT NOT NULL,
                    target_uuid     TEXT NOT NULL,
                    target_name     TEXT NOT NULL,
                    reason          TEXT NOT NULL,
                    details         TEXT,
                    server_id       TEXT NOT NULL,
                    status          TEXT NOT NULL DEFAULT 'OPEN',
                    staff_uuid      TEXT,
                    staff_note      TEXT,
                    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""";

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar tabela " + TABLE, e);
        }
    }

    public long insert(PlayerReport draft) {
        String sql = """
                INSERT INTO player_reports
                (reporter_uuid, reporter_name, target_uuid, target_name, reason, details,
                 server_id, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";
        Timestamp now = Timestamp.from(Instant.now());
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, draft.reporterId().toString());
            ps.setString(2, trim(draft.reporterName(), 16));
            ps.setString(3, draft.targetId().toString());
            ps.setString(4, trim(draft.targetName(), 16));
            ps.setString(5, draft.reason().id());
            ps.setString(6, draft.details());
            ps.setString(7, trim(draft.serverId(), 32));
            ps.setString(8, draft.status().name());
            ps.setTimestamp(9, now);
            ps.setTimestamp(10, now);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return -1L;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao inserir report", e);
        }
    }

    public boolean hasRecentDuplicate(UUID reporter, UUID target, long withinHours) {
        String sql = """
                SELECT 1 FROM player_reports
                WHERE reporter_uuid = ? AND target_uuid = ? AND status = 'OPEN'
                AND created_at >= ? LIMIT 1""";
        Instant since = Instant.now().minusSeconds(withinHours * 3600L);
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reporter.toString());
            ps.setString(2, target.toString());
            ps.setTimestamp(3, Timestamp.from(since));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public Optional<PlayerReport> findById(long id) {
        String sql = "SELECT * FROM player_reports WHERE id = ?";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar report " + id, e);
        }
        return Optional.empty();
    }

    public List<PlayerReport> listOpen(int limit) {
        String sql = """
                SELECT * FROM player_reports WHERE status = 'OPEN'
                ORDER BY created_at DESC LIMIT ?""";
        List<PlayerReport> list = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar reports", e);
        }
        return list;
    }

    public boolean close(long id, UUID staffId, String note, ReportStatus status) {
        String sql = """
                UPDATE player_reports SET status = ?, staff_uuid = ?, staff_note = ?, updated_at = ?
                WHERE id = ? AND status = 'OPEN'""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, staffId != null ? staffId.toString() : null);
            ps.setString(3, note);
            ps.setTimestamp(4, Timestamp.from(Instant.now()));
            ps.setLong(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao fechar report " + id, e);
        }
    }

    private static PlayerReport map(ResultSet rs) throws SQLException {
        ReportReason reason = ReportReason.fromInput(rs.getString("reason"));
        if (reason == null) reason = ReportReason.OTHER;
        UUID staff = null;
        String staffRaw = rs.getString("staff_uuid");
        if (staffRaw != null && !staffRaw.isBlank()) {
            try {
                staff = UUID.fromString(staffRaw);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return new PlayerReport(
                rs.getLong("id"),
                UUID.fromString(rs.getString("reporter_uuid")),
                rs.getString("reporter_name"),
                UUID.fromString(rs.getString("target_uuid")),
                rs.getString("target_name"),
                reason,
                rs.getString("details"),
                rs.getString("server_id"),
                ReportStatus.fromDb(rs.getString("status")),
                staff,
                rs.getString("staff_note"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private static String trim(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }
}
