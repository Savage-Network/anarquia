package com.minecraft.lobbyhost.backend.data;

import com.google.gson.reflect.TypeToken;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.rank.Rank;
import com.minecraft.lobbyhost.account.context.objects.rank.RankInfo;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.account.context.objects.tag.Tag;
import com.minecraft.lobbyhost.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Fonte de verdade para rank e tag da conta.
 *
 * <p>O JSON em {@code accounts.data} pode ficar desatualizado quando auth/lobby salvam
 * perfil com cache antigo. Ao carregar, estes valores sempre prevalecem.
 */
@RequiredArgsConstructor
public final class AccountIdentityData {

    private static final Type AVAILABLE_RANKS_TYPE = new TypeToken<List<Rank>>() {}.getType();

    private final HikariProvider database;

    public void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS account_identity (
                    player_id           VARCHAR(36) PRIMARY KEY,
                    primary_rank        VARCHAR(32)  NOT NULL DEFAULT 'MEMBER',
                    primary_given_at    BIGINT       NOT NULL DEFAULT 0,
                    primary_expires_at  BIGINT       NOT NULL DEFAULT 0,
                    available_ranks     TEXT         NOT NULL,
                    tag                 VARCHAR(64)  NOT NULL DEFAULT 'MEMBER',
                    updated_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
                )""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar account_identity", e);
        }
    }

    /** Aplica rank/tag autoritativos na conta (migra do JSON se ainda nao existir linha). */
    public void applyTo(Account account) {
        if (account == null) return;

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT primary_rank, primary_given_at, primary_expires_at, available_ranks, tag "
                             + "FROM account_identity WHERE player_id = ?")) {
            ps.setString(1, account.getId().toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    saveFrom(account);
                    return;
                }

                RankType type = parseRankType(rs.getString("primary_rank"));
                Rank primary = Rank.builder()
                        .type(type)
                        .givenAt(rs.getLong("primary_given_at"))
                        .expiresAt(rs.getLong("primary_expires_at"))
                        .build();

                List<Rank> available = parseAvailableRanks(rs.getString("available_ranks"));
                Tag tag = parseTag(rs.getString("tag"));

                RankInfo info = account.getContext().getRankInfo();
                info.setRank(primary);
                info.setAvailableRanks(new ArrayList<>(available));
                account.getContext().setTag(tag);
            }
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao carregar identidade de " + account.getId() + ": " + e.getMessage());
        }
    }

    /** Persiste rank/tag atuais — usado apenas em alteracoes staff ou sync autorizado. */
    public void saveFrom(Account account) {
        if (account == null) return;

        Rank primary = account.getRank();
        RankType type = primary != null && primary.getType() != null ? primary.getType() : RankType.MEMBER;
        long givenAt = primary != null && primary.getGivenAt() > 0
                ? primary.getGivenAt()
                : System.currentTimeMillis();
        long expiresAt = primary != null ? primary.getExpiresAt() : 0L;

        List<Rank> available = account.getAvailableRanks() != null
                ? new ArrayList<>(account.getAvailableRanks())
                : new ArrayList<>();
        Tag tag = account.getTag() != null ? account.getTag() : Tag.MEMBER;
        String availableJson = Core.GSON.toJson(available, AVAILABLE_RANKS_TYPE);

        String upsert = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                INSERT INTO account_identity
                    (player_id, primary_rank, primary_given_at, primary_expires_at, available_ranks, tag)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    primary_rank = VALUES(primary_rank),
                    primary_given_at = VALUES(primary_given_at),
                    primary_expires_at = VALUES(primary_expires_at),
                    available_ranks = VALUES(available_ranks),
                    tag = VALUES(tag),
                    updated_at = CURRENT_TIMESTAMP
                """
                : """
                INSERT INTO account_identity
                    (player_id, primary_rank, primary_given_at, primary_expires_at, available_ranks, tag)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(player_id) DO UPDATE SET
                    primary_rank = excluded.primary_rank,
                    primary_given_at = excluded.primary_given_at,
                    primary_expires_at = excluded.primary_expires_at,
                    available_ranks = excluded.available_ranks,
                    tag = excluded.tag,
                    updated_at = CURRENT_TIMESTAMP
                """;

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(upsert)) {
            ps.setString(1, account.getId().toString());
            ps.setString(2, type.name());
            ps.setLong(3, givenAt);
            ps.setLong(4, expiresAt);
            ps.setString(5, availableJson);
            ps.setString(6, tag.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao salvar identidade de " + account.getId() + ": " + e.getMessage());
        }
    }

    private static RankType parseRankType(String raw) {
        if (raw == null || raw.isBlank()) return RankType.MEMBER;
        try {
            return RankType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return RankType.MEMBER;
        }
    }

    private static Tag parseTag(String raw) {
        if (raw == null || raw.isBlank()) return Tag.MEMBER;
        try {
            return Tag.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            Tag byId = Tag.of(raw);
            return byId != null ? byId : Tag.MEMBER;
        }
    }

    private static List<Rank> parseAvailableRanks(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            List<Rank> ranks = Core.GSON.fromJson(json, AVAILABLE_RANKS_TYPE);
            return ranks != null ? new ArrayList<>(ranks) : new ArrayList<>();
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
    }
}
