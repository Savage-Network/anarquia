package com.minecraft.proxyhost.backend.data;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.account.context.AccountContext;
import com.minecraft.proxyhost.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Persistencia do {@link Account} - schema identico ao do plugin {@code game}
 * (tabela {@code accounts}, coluna {@code data} com JSON do {@link AccountContext}).
 */
@RequiredArgsConstructor
public final class AccountData {

    private final HikariProvider database;

    public void createTables() {
        String json = database.getType() == HikariProvider.DatabaseType.MYSQL ? "JSON" : "TEXT";
        String sql = """
                CREATE TABLE IF NOT EXISTS accounts (
                    id          VARCHAR(36) PRIMARY KEY,
                    name        VARCHAR(16) NOT NULL,
                    data        %s         NOT NULL,
                    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
                    updated_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
                )""".formatted(json);

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().error("Falha ao criar tabela accounts: " + e.getMessage());
            throw new RuntimeException(e);
        }

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "CREATE INDEX IF NOT EXISTS idx_accounts_name ON accounts(name)")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public Optional<Account> load(UUID id) {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT name, data FROM accounts WHERE id = ?")) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                String name = rs.getString("name");
                String json = rs.getString("data");
                AccountContext ctx = Core.GSON.fromJson(json, AccountContext.class);
                Account account = new Account(id, name);
                account.replaceContext(ctx != null ? ctx : new AccountContext());
                return Optional.of(account);
            }
        } catch (SQLException e) {
            Core.getLogger().error("Erro ao carregar conta " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public void save(Account account) {
        String json = Core.GSON.toJson(account.getContext());
        String upsert = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? "INSERT INTO accounts (id, name, data) VALUES (?, ?, ?) " +
                  "ON DUPLICATE KEY UPDATE name = VALUES(name), data = VALUES(data), updated_at = CURRENT_TIMESTAMP"
                : "INSERT INTO accounts (id, name, data) VALUES (?, ?, ?) " +
                  "ON CONFLICT(id) DO UPDATE SET name = excluded.name, data = excluded.data, updated_at = CURRENT_TIMESTAMP";

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(upsert)) {
            ps.setString(1, account.getId().toString());
            ps.setString(2, account.getName());
            ps.setString(3, json);
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().error("Erro ao salvar conta " + account.getId() + ": " + e.getMessage());
        }
    }

    public CompletableFuture<Optional<Account>> loadAsync(UUID id) {
        return CompletableFuture.supplyAsync(() -> load(id));
    }

    public CompletableFuture<Void> saveAsync(Account account) {
        return CompletableFuture.runAsync(() -> save(account));
    }

    public CompletableFuture<Void> updateName(UUID id, String name) {
        return CompletableFuture.runAsync(() -> {
            try (Connection c = database.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "UPDATE accounts SET name = ?, updated_at = ? WHERE id = ?")) {
                ps.setString(1, name);
                ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                ps.setString(3, id.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                Core.getLogger().error("Erro ao atualizar nome da conta: " + e.getMessage());
            }
        });
    }

    /** Carregamento sincrono por nome (best-effort - usado por /account em offline). */
    public Optional<Account> loadByName(String name) {
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, name, data FROM accounts WHERE LOWER(name) = ? LIMIT 1")) {
            ps.setString(1, name.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                UUID id = UUID.fromString(rs.getString("id"));
                String n = rs.getString("name");
                String json = rs.getString("data");
                AccountContext ctx = Core.GSON.fromJson(json, AccountContext.class);
                Account a = new Account(id, n);
                a.replaceContext(ctx != null ? ctx : new AccountContext());
                return Optional.of(a);
            }
        } catch (SQLException e) {
            Core.getLogger().error("Erro ao carregar conta por nome " + name + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}
