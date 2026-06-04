package com.minecraft.game.backend.data;

import com.minecraft.game.Core;
import com.minecraft.game.api.auction.AuctionListing;
import com.minecraft.game.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public final class AuctionData {

    private final HikariProvider database;
    private final Map<UUID, AuctionListing> cache = new ConcurrentHashMap<>();

    public void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS auction_listings (
                    id            VARCHAR(36) PRIMARY KEY,
                    seller_id     VARCHAR(36) NOT NULL,
                    seller_name   VARCHAR(16) NOT NULL,
                    material      VARCHAR(64) NOT NULL,
                    amount        INT NOT NULL,
                    price         INT NOT NULL,
                    created_at    BIGINT NOT NULL,
                    expires_at    BIGINT NOT NULL
                )""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar auction_listings", e);
        }
    }

    public void loadAll() {
        cache.clear();
        purgeExpired();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM auction_listings");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AuctionListing l = map(rs);
                if (!l.isExpired()) cache.put(l.getId(), l);
            }
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao carregar leilao: " + e.getMessage());
        }
    }

    public List<AuctionListing> active() {
        purgeExpired();
        return new ArrayList<>(cache.values());
    }

    public List<AuctionListing> bySeller(UUID seller) {
        return active().stream().filter(l -> l.getSellerId().equals(seller)).toList();
    }

    public Optional<AuctionListing> get(UUID id) {
        AuctionListing l = cache.get(id);
        if (l != null && l.isExpired()) {
            cache.remove(id);
            delete(id);
            return Optional.empty();
        }
        return Optional.ofNullable(l);
    }

    public void save(AuctionListing listing) {
        cache.put(listing.getId(), listing);
        String upsert = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                INSERT INTO auction_listings (id, seller_id, seller_name, material, amount, price, created_at, expires_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE price = VALUES(price), amount = VALUES(amount)"""
                : """
                INSERT INTO auction_listings (id, seller_id, seller_name, material, amount, price, created_at, expires_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET price = excluded.price, amount = excluded.amount""";

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(upsert)) {
            bind(ps, listing);
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().warning("Erro ao salvar leilao: " + e.getMessage());
        }
    }

    public void delete(UUID id) {
        cache.remove(id);
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM auction_listings WHERE id = ?")) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException ignored) { }
    }

    private void purgeExpired() {
        long now = System.currentTimeMillis();
        cache.values().removeIf(l -> {
            if (l.getExpiresAt() <= now) {
                delete(l.getId());
                return true;
            }
            return false;
        });
    }

    private static AuctionListing map(ResultSet rs) throws SQLException {
        Material mat = Material.matchMaterial(rs.getString("material"));
        if (mat == null) mat = Material.STONE;
        return new AuctionListing(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                rs.getString("seller_name"),
                mat,
                rs.getInt("amount"),
                rs.getInt("price"),
                rs.getLong("created_at"),
                rs.getLong("expires_at")
        );
    }

    private static void bind(PreparedStatement ps, AuctionListing l) throws SQLException {
        ps.setString(1, l.getId().toString());
        ps.setString(2, l.getSellerId().toString());
        ps.setString(3, l.getSellerName());
        ps.setString(4, l.getMaterial().name());
        ps.setInt(5, l.getAmount());
        ps.setInt(6, l.getPrice());
        ps.setLong(7, l.getCreatedAt());
        ps.setLong(8, l.getExpiresAt());
    }
}
