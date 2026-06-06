package com.minecraft.game.backend.data;

import com.minecraft.game.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class ClanMarketData {

    public static final String TABLE = "clan_market_listings";

    public record Listing(String id, UUID clanId, UUID sellerId, String sellerName,
                          String material, int amount, int price, long createdAt) {}

    private final HikariProvider database;

    public void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS clan_market_listings (
                    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
                    clan_id       VARCHAR(36)  NOT NULL,
                    seller_id     VARCHAR(36)  NOT NULL,
                    seller_name   VARCHAR(16)  NOT NULL,
                    material      VARCHAR(64)  NOT NULL,
                    amount        INT          NOT NULL,
                    price         INT          NOT NULL,
                    created_at    BIGINT       NOT NULL
                )""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar " + TABLE, e);
        }
    }

    public void insert(Listing listing) {
        String sql = """
                INSERT INTO clan_market_listings
                (id, clan_id, seller_id, seller_name, material, amount, price, created_at)
                VALUES (?,?,?,?,?,?,?,?)
                """;
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, listing.id());
            ps.setString(2, listing.clanId().toString());
            ps.setString(3, listing.sellerId().toString());
            ps.setString(4, listing.sellerName());
            ps.setString(5, listing.material());
            ps.setInt(6, listing.amount());
            ps.setInt(7, listing.price());
            ps.setLong(8, listing.createdAt());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao inserir listing clã", e);
        }
    }

    public void delete(String id) {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM clan_market_listings WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao remover listing clã", e);
        }
    }

    public Optional<Listing> find(String id) {
        String sql = "SELECT * FROM clan_market_listings WHERE id = ? LIMIT 1";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar listing clã", e);
        }
        return Optional.empty();
    }

    public List<Listing> listByClan(UUID clanId, int limit) {
        String sql = "SELECT * FROM clan_market_listings WHERE clan_id = ? ORDER BY created_at DESC LIMIT ?";
        List<Listing> out = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, clanId.toString());
            ps.setInt(2, Math.max(1, Math.min(limit, 54)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar mercado clã", e);
        }
        return out;
    }

    public int countByClan(UUID clanId) {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM clan_market_listings WHERE clan_id = ?")) {
            ps.setString(1, clanId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao contar mercado clã", e);
        }
        return 0;
    }

    private static Listing map(ResultSet rs) throws SQLException {
        return new Listing(
                rs.getString("id"),
                UUID.fromString(rs.getString("clan_id")),
                UUID.fromString(rs.getString("seller_id")),
                rs.getString("seller_name"),
                rs.getString("material"),
                rs.getInt("amount"),
                rs.getInt("price"),
                rs.getLong("created_at"));
    }
}
