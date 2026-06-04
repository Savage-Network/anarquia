package com.minecraft.game.backend.data;

import com.minecraft.game.Core;
import com.minecraft.game.api.shop.SignShop;
import com.minecraft.game.api.shop.SignShopType;
import com.minecraft.game.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public final class SignShopData {

    private final HikariProvider database;
    private final Map<String, SignShop> cache = new ConcurrentHashMap<>();

    public void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS sign_shops (
                    id            VARCHAR(80) PRIMARY KEY,
                    world         VARCHAR(64) NOT NULL,
                    x             INT NOT NULL,
                    y             INT NOT NULL,
                    z             INT NOT NULL,
                    owner_id      VARCHAR(36) NOT NULL,
                    type          VARCHAR(8) NOT NULL,
                    material      VARCHAR(64) NOT NULL,
                    price         INT NOT NULL,
                    server_shop   TINYINT(1) DEFAULT 0,
                    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""";

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar tabela sign_shops", e);
        }
    }

    public void loadAll() {
        cache.clear();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM sign_shops");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SignShop shop = mapRow(rs);
                cache.put(shop.getId(), shop);
            }
            Core.getLogger().info("Lojas de placa carregadas: " + cache.size());
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao carregar sign_shops: " + e.getMessage());
        }
    }

    public Optional<SignShop> get(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    public void save(SignShop shop) {
        cache.put(shop.getId(), shop);
        String upsert = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                INSERT INTO sign_shops (id, world, x, y, z, owner_id, type, material, price, server_shop)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE owner_id = VALUES(owner_id), type = VALUES(type),
                material = VALUES(material), price = VALUES(price), server_shop = VALUES(server_shop)"""
                : """
                INSERT INTO sign_shops (id, world, x, y, z, owner_id, type, material, price, server_shop)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET owner_id = excluded.owner_id, type = excluded.type,
                material = excluded.material, price = excluded.price, server_shop = excluded.server_shop""";

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(upsert)) {
            ps.setString(1, shop.getId());
            ps.setString(2, shop.getWorld());
            ps.setInt(3, shop.getX());
            ps.setInt(4, shop.getY());
            ps.setInt(5, shop.getZ());
            ps.setString(6, shop.getOwnerId().toString());
            ps.setString(7, shop.getType().name());
            ps.setString(8, shop.getMaterial().name());
            ps.setInt(9, shop.getPricePerUnit());
            ps.setInt(10, shop.isServerShop() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao salvar loja " + shop.getId() + ": " + e.getMessage());
        }
    }

    public void delete(String id) {
        cache.remove(id);
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM sign_shops WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().warning("Erro ao remover loja " + id + ": " + e.getMessage());
        }
    }

    private static SignShop mapRow(ResultSet rs) throws SQLException {
        Material mat = Material.matchMaterial(rs.getString("material"));
        if (mat == null) mat = Material.STONE;
        return new SignShop(
                rs.getString("id"),
                rs.getString("world"),
                rs.getInt("x"),
                rs.getInt("y"),
                rs.getInt("z"),
                UUID.fromString(rs.getString("owner_id")),
                SignShopType.valueOf(rs.getString("type")),
                mat,
                rs.getInt("price"),
                rs.getInt("server_shop") == 1
        );
    }
}
