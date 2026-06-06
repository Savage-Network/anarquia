package com.minecraft.game.backend.data;

import com.minecraft.game.Core;
import com.minecraft.game.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * Codigos temporarios e vinculos Minecraft ↔ Discord.
 */
@RequiredArgsConstructor
public final class DiscordLinkData {

    private static final long CODE_TTL_MS = 600_000L;
    private static final String CODE_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final HikariProvider database;

    public void createTables() {
        String codes = """
                CREATE TABLE IF NOT EXISTS discord_link_codes (
                    code         VARCHAR(8)  PRIMARY KEY,
                    player_id    VARCHAR(36) NOT NULL,
                    player_name  VARCHAR(16) NOT NULL,
                    created_at   BIGINT      NOT NULL,
                    expires_at   BIGINT      NOT NULL
                )""";
        String links = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                CREATE TABLE IF NOT EXISTS discord_links (
                    player_id    VARCHAR(36) PRIMARY KEY,
                    player_name  VARCHAR(16) NOT NULL,
                    discord_id   VARCHAR(32) NOT NULL,
                    discord_tag  VARCHAR(128),
                    linked_at    BIGINT      NOT NULL,
                    UNIQUE KEY uk_discord_links_discord (discord_id)
                )"""
                : """
                CREATE TABLE IF NOT EXISTS discord_links (
                    player_id    VARCHAR(36) PRIMARY KEY,
                    player_name  VARCHAR(16) NOT NULL,
                    discord_id   VARCHAR(32) NOT NULL UNIQUE,
                    discord_tag  VARCHAR(128),
                    linked_at    BIGINT      NOT NULL
                )""";

        try (Connection c = database.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(codes)) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(links)) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar tabelas discord_links", e);
        }

        index(database, "idx_discord_codes_player", "discord_link_codes(player_id)");
        index(database, "idx_discord_links_discord", "discord_links(discord_id)");
    }

    public Optional<LinkRecord> findLinkByPlayer(UUID playerId) {
        if (playerId == null) return Optional.empty();
        String sql = """
                SELECT player_id, player_name, discord_id, discord_tag, linked_at
                FROM discord_links WHERE player_id = ? LIMIT 1""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(readLink(rs));
            }
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao buscar vinculo do jogador: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<LinkRecord> findLinkByDiscord(String discordId) {
        if (discordId == null || discordId.isBlank()) return Optional.empty();
        String sql = """
                SELECT player_id, player_name, discord_id, discord_tag, linked_at
                FROM discord_links WHERE discord_id = ? LIMIT 1""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, discordId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(readLink(rs));
            }
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao buscar vinculo do Discord: " + e.getMessage());
            return Optional.empty();
        }
    }

    public String createCode(UUID playerId, String playerName) {
        invalidateCodesFor(playerId);
        long now = System.currentTimeMillis();
        long expiresAt = now + CODE_TTL_MS;

        for (int attempt = 0; attempt < 12; attempt++) {
            String code = randomCode();
            String sql = """
                    INSERT INTO discord_link_codes (code, player_id, player_name, created_at, expires_at)
                    VALUES (?, ?, ?, ?, ?)""";
            try (Connection c = database.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, code);
                ps.setString(2, playerId.toString());
                ps.setString(3, playerName);
                ps.setLong(4, now);
                ps.setLong(5, expiresAt);
                ps.executeUpdate();
                return code;
            } catch (SQLException e) {
                if (isDuplicate(e)) continue;
                throw new RuntimeException("Falha ao gerar codigo de vinculo", e);
            }
        }
        throw new RuntimeException("Nao foi possivel gerar codigo de vinculo");
    }

    public void invalidateCodesFor(UUID playerId) {
        if (playerId == null) return;
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM discord_link_codes WHERE player_id = ?")) {
            ps.setString(1, playerId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().warning("Erro ao invalidar codigos: " + e.getMessage());
        }
    }

    public long codeTtlMs() {
        return CODE_TTL_MS;
    }

    private static LinkRecord readLink(ResultSet rs) throws SQLException {
        return new LinkRecord(
                UUID.fromString(rs.getString("player_id")),
                rs.getString("player_name"),
                rs.getString("discord_id"),
                rs.getString("discord_tag"),
                rs.getLong("linked_at"));
    }

    private static String randomCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private static boolean isDuplicate(SQLException e) {
        String state = e.getSQLState();
        return "23000".equals(state) || "23505".equals(state);
    }

    private static void index(HikariProvider database, String name, String onClause) {
        String sql = "CREATE INDEX IF NOT EXISTS " + name + " ON " + onClause;
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().warning("Indice " + name + " nao criado: " + e.getMessage());
        }
    }

    public record LinkRecord(
            UUID playerId,
            String playerName,
            String discordId,
            String discordTag,
            long linkedAt) {
    }
}
