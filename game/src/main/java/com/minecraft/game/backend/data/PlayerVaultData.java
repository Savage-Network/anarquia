package com.minecraft.game.backend.data;

import com.minecraft.game.Core;
import com.minecraft.game.backend.database.HikariProvider;
import com.minecraft.game.util.ItemStackCodec;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public final class PlayerVaultData {

    public static final int BAU_SIZE = 54;
    public static final int EC_SIZE = 27;

    private final HikariProvider database;

    public void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS player_vaults (
                    player_id   VARCHAR(36) PRIMARY KEY,
                    bau_data    TEXT,
                    ec_data     TEXT
                )""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar player_vaults", e);
        }
    }

    public ItemStack[] loadBau(UUID playerId) {
        return loadColumn(playerId, "bau_data", BAU_SIZE);
    }

    public ItemStack[] loadEc(UUID playerId) {
        return loadColumn(playerId, "ec_data", EC_SIZE);
    }

    private ItemStack[] loadColumn(UUID playerId, String column, int size) {
        String sql = "SELECT " + column + " FROM player_vaults WHERE player_id = ?";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, playerId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return new ItemStack[size];
                return ItemStackCodec.decode(rs.getString(column), size);
            }
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao carregar " + column + " de " + playerId + ": " + e.getMessage());
            return new ItemStack[size];
        }
    }

    public void saveBau(UUID playerId, ItemStack[] contents) {
        saveColumn(playerId, "bau_data", contents);
    }

    public void saveEc(UUID playerId, ItemStack[] contents) {
        saveColumn(playerId, "ec_data", contents);
    }

    private void saveColumn(UUID playerId, String column, ItemStack[] contents) {
        String encoded = ItemStackCodec.encode(contents);
        String upsert = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? "INSERT INTO player_vaults (player_id, " + column + ") VALUES (?, ?) "
                  + "ON DUPLICATE KEY UPDATE " + column + " = VALUES(" + column + ")"
                : "INSERT INTO player_vaults (player_id, " + column + ") VALUES (?, ?) "
                  + "ON CONFLICT(player_id) DO UPDATE SET " + column + " = excluded." + column;

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(upsert)) {
            ps.setString(1, playerId.toString());
            ps.setString(2, encoded);
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao salvar " + column + " de " + playerId + ": " + e.getMessage());
        }
    }

    public CompletableFuture<Void> saveBauAsync(UUID playerId, ItemStack[] contents) {
        return CompletableFuture.runAsync(() -> saveBau(playerId, contents));
    }

    public CompletableFuture<Void> saveEcAsync(UUID playerId, ItemStack[] contents) {
        return CompletableFuture.runAsync(() -> saveEc(playerId, contents));
    }
}
