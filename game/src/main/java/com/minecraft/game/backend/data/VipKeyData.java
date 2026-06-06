package com.minecraft.game.backend.data;

import com.minecraft.game.Core;
import com.minecraft.game.account.context.objects.rank.RankType;
import com.minecraft.game.api.vipkey.VipKey;
import com.minecraft.game.api.vipkey.VipKeyFormat;
import com.minecraft.game.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class VipKeyData {

    private final HikariProvider database;

    public void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS vip_keys (
                    id               VARCHAR(36) PRIMARY KEY,
                    code             VARCHAR(48) NOT NULL UNIQUE,
                    rank_type        VARCHAR(32) NOT NULL,
                    duration_ms      BIGINT NOT NULL,
                    created_by       VARCHAR(36),
                    created_by_name  VARCHAR(32),
                    created_at       BIGINT NOT NULL,
                    redeemed_by      VARCHAR(36),
                    redeemed_by_name VARCHAR(32),
                    redeemed_at      BIGINT DEFAULT 0
                )""";

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar tabela vip_keys", e);
        }
    }

    public VipKey insert(VipKey key) {
        String sql = """
                INSERT INTO vip_keys (id, code, rank_type, duration_ms, created_by, created_by_name, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key.getId().toString());
            ps.setString(2, key.getCode());
            ps.setString(3, key.getRankType().name());
            ps.setLong(4, key.getDurationMs());
            ps.setString(5, key.getCreatedBy() != null ? key.getCreatedBy().toString() : null);
            ps.setString(6, key.getCreatedByName());
            ps.setLong(7, key.getCreatedAt());
            ps.executeUpdate();
            return key;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao salvar vip key", e);
        }
    }

    public Optional<VipKey> findByCode(String code) {
        if (code == null || code.isBlank()) return Optional.empty();

        String normalized = VipKeyFormat.normalize(code);
        Optional<VipKey> found = findByCodeExact(normalized);
        if (found.isPresent()) return found;

        String legacy = code.trim().toUpperCase(Locale.ROOT);
        if (!legacy.equals(normalized)) {
            return findByCodeExact(legacy);
        }
        return Optional.empty();
    }

    private Optional<VipKey> findByCodeExact(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        String sql = "SELECT * FROM vip_keys WHERE UPPER(code) = ? LIMIT 1";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao buscar vip key: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean markRedeemed(String code, UUID playerId, String playerName, long redeemedAt) {
        Optional<VipKey> key = findByCode(code);
        if (key.isEmpty()) return false;

        String canonical = key.get().getCode();
        String sql = """
                UPDATE vip_keys
                SET redeemed_by = ?, redeemed_by_name = ?, redeemed_at = ?
                WHERE UPPER(code) = ? AND (redeemed_at IS NULL OR redeemed_at = 0)""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            ps.setString(2, playerName);
            ps.setLong(3, redeemedAt);
            ps.setString(4, canonical.toUpperCase(Locale.ROOT));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao resgatar vip key: " + e.getMessage());
            return false;
        }
    }

    public List<VipKey> listUnredeemed(int limit) {
        List<VipKey> list = new ArrayList<>();
        String sql = """
                SELECT * FROM vip_keys
                WHERE redeemed_at IS NULL OR redeemed_at = 0
                ORDER BY created_at DESC
                LIMIT ?""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao listar vip keys: " + e.getMessage());
        }
        return list;
    }

    private static VipKey mapRow(ResultSet rs) throws SQLException {
        RankType type = RankType.valueOf(rs.getString("rank_type"));
        UUID createdBy = parseUuid(rs.getString("created_by"));
        UUID redeemedBy = parseUuid(rs.getString("redeemed_by"));
        return new VipKey(
                UUID.fromString(rs.getString("id")),
                rs.getString("code"),
                type,
                rs.getLong("duration_ms"),
                createdBy,
                rs.getString("created_by_name"),
                rs.getLong("created_at"),
                redeemedBy,
                rs.getString("redeemed_by_name"),
                rs.getLong("redeemed_at")
        );
    }

    private static UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
