package com.minecraft.auth.backend.data;

import com.minecraft.auth.Core;
import com.minecraft.auth.account.Account;
import com.minecraft.auth.account.context.objects.auth.AuthMetadata;
import com.minecraft.auth.backend.database.HikariProvider;
import com.minecraft.auth.backend.model.PlayerAuth;
import com.minecraft.auth.util.AuthAccountSanitizer;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class AuthData {

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

    public Optional<PlayerAuth> load(UUID playerId) {
        String sql = "SELECT * FROM player_auth WHERE player_id = ?";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao carregar auth de " + playerId, e);
        }
    }

    public boolean exists(UUID playerId) {
        String sql = "SELECT 1 FROM player_auth WHERE player_id = ? LIMIT 1";
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

    public void save(PlayerAuth auth) {
        String upsert = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                INSERT INTO player_auth
                (player_id, password_hash, last_password_hash, recent_hashes, registered_at, last_login_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    password_hash = VALUES(password_hash),
                    last_password_hash = VALUES(last_password_hash),
                    recent_hashes = VALUES(recent_hashes),
                    registered_at = COALESCE(player_auth.registered_at, VALUES(registered_at)),
                    last_login_at = VALUES(last_login_at),
                    updated_at = VALUES(updated_at)
                """
                : """
                INSERT INTO player_auth
                (player_id, password_hash, last_password_hash, recent_hashes, registered_at, last_login_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(player_id) DO UPDATE SET
                    password_hash = excluded.password_hash,
                    last_password_hash = excluded.last_password_hash,
                    recent_hashes = excluded.recent_hashes,
                    registered_at = COALESCE(player_auth.registered_at, excluded.registered_at),
                    last_login_at = excluded.last_login_at,
                    updated_at = excluded.updated_at
                """;

        Timestamp now = Timestamp.from(Instant.now());
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(upsert)) {
            ps.setString(1, auth.getPlayerId().toString());
            ps.setString(2, nullToEmpty(auth.getPasswordHash()));
            ps.setString(3, auth.getLastPasswordHash());
            ps.setString(4, Core.GSON.toJson(auth.getRecentHashes() != null
                    ? auth.getRecentHashes() : List.of()));
            ps.setTimestamp(5, auth.getRegisteredAt() != null
                    ? Timestamp.from(auth.getRegisteredAt()) : null);
            ps.setTimestamp(6, auth.getLastLoginAt() != null
                    ? Timestamp.from(auth.getLastLoginAt()) : null);
            ps.setTimestamp(7, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao salvar auth de " + auth.getPlayerId(), e);
        }
    }

    /**
     * Move senha do JSON legado ({@code accounts.data}) para {@code player_auth}.
     */
    public Optional<PlayerAuth> migrateFromContextIfNeeded(Account account) {
        if (account == null) return Optional.empty();
        if (exists(account.getId())) {
            return load(account.getId());
        }
        AuthMetadata meta = account.getContext().getAuth();
        if (meta == null || meta.getPassword() == null || meta.getPassword().isBlank()) {
            return Optional.empty();
        }

        PlayerAuth auth = PlayerAuth.fromLegacy(account.getId(), meta);
        save(auth);
        AuthAccountSanitizer.stripSecrets(meta);
        meta.setState(auth.persistedState());
        account.markDirty();
        Core.getLogger().info("[auth] Migrado player_auth para " + account.getName());
        return Optional.of(auth);
    }

    public int countRegistered() {
        String sql = "SELECT COUNT(*) FROM player_auth WHERE password_hash <> ''";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    private static PlayerAuth map(ResultSet rs) throws SQLException {
        PlayerAuth auth = PlayerAuth.empty(UUID.fromString(rs.getString("player_id")));
        auth.setPasswordHash(rs.getString("password_hash"));
        auth.setLastPasswordHash(rs.getString("last_password_hash"));
        String recentJson = rs.getString("recent_hashes");
        if (recentJson != null && !recentJson.isBlank()) {
            String[] arr = Core.GSON.fromJson(recentJson, String[].class);
            auth.setRecentHashes(arr != null ? new ArrayList<>(Arrays.asList(arr)) : new ArrayList<>());
        }
        Timestamp reg = rs.getTimestamp("registered_at");
        if (reg != null) auth.setRegisteredAt(reg.toInstant());
        Timestamp login = rs.getTimestamp("last_login_at");
        if (login != null) auth.setLastLoginAt(login.toInstant());
        return auth;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
