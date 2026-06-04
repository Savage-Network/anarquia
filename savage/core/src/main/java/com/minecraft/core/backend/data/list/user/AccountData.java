package com.minecraft.core.backend.data.list.user;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.backend.data.template.UserTemplate;
import com.minecraft.core.backend.database.postgresql.PostgresDatabase;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.backend.database.redis.message.types.account.AccountUpdateMessage;
import com.minecraft.core.controller.list.AccountController;
import com.minecraft.core.util.list.JsonUtil;
import com.google.gson.JsonObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class AccountData implements UserTemplate<Account> {

    private final PostgresDatabase postgres;
    private final RedisDatabase redis;

    private final AccountController controller;

    private final String ACCOUNT_KEY_FORMAT = "account:";

    public AccountData(PostgresDatabase postgres, RedisDatabase redis) {
        this.postgres = postgres;
        this.redis = redis;
        this.controller = Core.getAccountController();
        createTable(postgres);
    }

    public static void createTable(PostgresDatabase postgres) {
        try (Connection conn = postgres.getConnection()) {
            // Criar tabela se não existir (sem as colunas id e name inicialmente)
            try (PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS accounts (
                    data JSONB NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """)) {
                stmt.execute();
            }
            
            // Adicionar coluna id se não existir
            try (PreparedStatement checkStmt = conn.prepareStatement("""
                SELECT column_name FROM information_schema.columns 
                WHERE table_name = 'accounts' AND column_name = 'id';
                """)) {
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    // Coluna não existe, criar
                    try (PreparedStatement stmt = conn.prepareStatement("""
                        ALTER TABLE accounts ADD COLUMN id VARCHAR(36);
                        """)) {
                        stmt.execute();
                    }
                }
            }
            
            // Atualizar valores nulos da coluna id
            try (PreparedStatement stmt = conn.prepareStatement("""
                UPDATE accounts SET id = data->>'id' WHERE id IS NULL;
                """)) {
                stmt.execute();
            } catch (SQLException e) {
                // Ignorar se houver erro
            }
            
            // Adicionar PRIMARY KEY se não existir
            try (PreparedStatement stmt = conn.prepareStatement("""
                ALTER TABLE accounts ADD PRIMARY KEY (id);
                """)) {
                stmt.execute();
            } catch (SQLException e) {
                // Primary key já existe, ignorar
            }
            
            // Adicionar coluna name se não existir
            try (PreparedStatement checkStmt = conn.prepareStatement("""
                SELECT column_name FROM information_schema.columns 
                WHERE table_name = 'accounts' AND column_name = 'name';
                """)) {
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    // Coluna não existe, criar
                    try (PreparedStatement stmt = conn.prepareStatement("""
                        ALTER TABLE accounts ADD COLUMN name VARCHAR(16);
                        """)) {
                        stmt.execute();
                    }
                }
            }
            
            // Atualizar valores nulos da coluna name
            try (PreparedStatement stmt = conn.prepareStatement("""
                UPDATE accounts SET name = data->>'name' WHERE name IS NULL;
                """)) {
                stmt.execute();
            } catch (SQLException e) {
                // Ignorar se houver erro
            }
            
            // Criar índices
            try (PreparedStatement stmt = conn.prepareStatement("""
                CREATE INDEX IF NOT EXISTS idx_accounts_name ON accounts(LOWER(name));
                """)) {
                stmt.execute();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement("""
                CREATE INDEX IF NOT EXISTS idx_accounts_ip ON accounts((data->>'context.ipAddress'));
                """)) {
                stmt.execute();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Account save(Account account) {
        String sql = "SELECT data FROM accounts WHERE id = ?";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, account.getId().toString());
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                // Conta não existe, criar nova
                String insertSql = "INSERT INTO accounts (id, name, data) VALUES (?, ?, ?::jsonb)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, account.getId().toString());
                    insertStmt.setString(2, account.getName());
                    insertStmt.setString(3, Core.GSON.toJson(account));
                    insertStmt.executeUpdate();
                }
                
                controller.save(account);
            } else {
                // Conta existe, carregar do banco
                String jsonData = rs.getString("data");
                account = Core.GSON.fromJson(jsonData, Account.class);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return account;
    }

    @Override
    public void delete(Account account) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, account.getId().toString());
            int deleted = stmt.executeUpdate();
            
            if (deleted > 0) {
                controller.remove(account.getId());
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            Core.getLogger().severe("[Account-Delete] Erro ao deletar conta de " + account.getName());
        }
    }

    public void clearCache() {
        controller.getCache().clear();

        redis.removeAll(ACCOUNT_KEY_FORMAT);
    }

    @Override
    public void update(Account account, String field) {
        try {
            JsonObject tree = JsonUtil.jsonTree(account);
            String sql = "UPDATE accounts SET data = ?::jsonb WHERE id = ?";
            
            try (Connection conn = postgres.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, Core.GSON.toJson(account));
                stmt.setString(2, account.getId().toString());
                
                int updated = stmt.executeUpdate();
                
                if (updated > 0) {
                    new AccountUpdateMessage(account, field, tree.get(field)).send();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startExpiration(Account account) {
        controller.remove(account.getId());

        redis.cache(ACCOUNT_KEY_FORMAT + account.getId(), 80);
    }

    public void removeCache(Account account) {
        controller.remove(account.getId());

        redis.delete(ACCOUNT_KEY_FORMAT + account.getId());
    }

    @Override
    public void cancelExpiration(Account account) {
        redis.persist(ACCOUNT_KEY_FORMAT + account.getId());
    }

    @Override
    public Account of(UUID id, boolean storeInRedis) {
        Account account = controller.of(id);

        if (account == null) {
            // Carregar direto do PostgreSQL
            String sql = "SELECT data FROM accounts WHERE id = ?";
            
            try (Connection conn = postgres.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, id.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String jsonData = rs.getString("data");
                    account = Core.GSON.fromJson(jsonData, Account.class);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return account;
    }

    @Override
    public Account of(String name) {
        Account account = controller.of(name);

        if (account == null) {
            String sql = "SELECT data FROM accounts WHERE LOWER(name) = LOWER(?)";
            
            try (Connection conn = postgres.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String jsonData = rs.getString("data");
                    account = Core.GSON.fromJson(jsonData, Account.class);
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return account;
    }

    public List<Account> getAlts(String ipAddress) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT data FROM accounts WHERE data->>'context.ipAddress' = ?";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, ipAddress);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String jsonData = rs.getString("data");
                Account account = Core.GSON.fromJson(jsonData, Account.class);
                accounts.add(account);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return accounts;
    }

    public boolean isExceededAccountLimit(String name, String ipAddress) {
        final int altSize = getAlts(ipAddress).size(),
                accountLimit = 3;

        Account existingAccount = of(name);

        return existingAccount == null && altSize >= accountLimit;
    }

    @Override
    public List<Account> list() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT data FROM accounts";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String jsonData = rs.getString("data");
                Account account = Core.GSON.fromJson(jsonData, Account.class);
                accounts.add(account);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return accounts;
    }

    @Override
    public Collection<Account> ranking(String field, int limit) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT data FROM accounts ORDER BY (data->>?)::numeric DESC LIMIT ?";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, field);
            stmt.setInt(2, limit * 2);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String jsonData = rs.getString("data");
                Account account = Core.GSON.fromJson(jsonData, Account.class);
                accounts.add(account);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return accounts;
    }
}
