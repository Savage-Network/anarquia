package com.minecraft.core.backend.data.list.user.arcade;

import com.minecraft.core.Core;
import com.minecraft.core.backend.database.postgresql.PostgresDatabase;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.controller.list.MemberController;
import com.minecraft.core.member.Member;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MemberData<T extends Member> {

    private final PostgresDatabase postgres;
    private final RedisDatabase redis;
    private final String tableName;

    private final MemberController controller;

    private final Type type;
    private final Class<T> generic;

    private final String MEMBER_KEY_FORMAT;

    protected final static List<String> MEMBER_TABLE_NAMES = Arrays.asList(
            "members_duels", "members_bedwars", "members_pvp", "members_skywars");

    @SuppressWarnings("unchecked")
    public MemberData(PostgresDatabase postgres, RedisDatabase redis, String tableName) {
        this.postgres = postgres;
        this.tableName = tableName;
        this.redis = redis;
        this.controller = Core.getMemberController();

        this.type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.generic = (Class<T>) type;

        MEMBER_KEY_FORMAT = "member:" + tableName + ":";
        
        // Criar tabela se não existir
        createTable(postgres, tableName);
    }
    
    /**
     * Cria a tabela de members no PostgreSQL se ela não existir
     * @param postgres Instância do PostgresDatabase
     * @param tableName Nome da tabela a ser criada
     */
    public static void createTable(PostgresDatabase postgres, String tableName) {
        try (Connection conn = postgres.getConnection()) {
            // Verificar se a tabela existe e tem a estrutura correta
            boolean tableExists = false;
            boolean hasAccountIdColumn = false;
            
            try (PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT column_name FROM information_schema.columns WHERE table_name = ? AND column_name = 'account_id'"
            )) {
                checkStmt.setString(1, tableName);
                ResultSet rs = checkStmt.executeQuery();
                hasAccountIdColumn = rs.next();
            }
            
            try (PreparedStatement checkTableStmt = conn.prepareStatement(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = ?)"
            )) {
                checkTableStmt.setString(1, tableName);
                ResultSet rs = checkTableStmt.executeQuery();
                if (rs.next()) {
                    tableExists = rs.getBoolean(1);
                }
            }
            
            // Se a tabela existe mas não tem account_id, dropar e recriar
            if (tableExists && !hasAccountIdColumn) {
                Core.getLogger().warning("[MemberData] Tabela '" + tableName + "' existe com estrutura antiga. Recriando...");
                try (PreparedStatement dropStmt = conn.prepareStatement("DROP TABLE IF EXISTS " + tableName + " CASCADE")) {
                    dropStmt.execute();
                }
            }
            
            // Create table
            try (PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "    id VARCHAR(36) PRIMARY KEY," +
                "    account_id VARCHAR(36) NOT NULL," +
                "    data JSONB NOT NULL," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            )) {
                stmt.execute();
            }
            
            // Create index
            try (PreparedStatement stmt = conn.prepareStatement(
                "CREATE INDEX IF NOT EXISTS idx_" + tableName + "_account ON " + tableName + "(account_id)"
            )) {
                stmt.execute();
            }
            
            Core.getLogger().info("[MemberData] Tabela '" + tableName + "' verificada/criada com sucesso.");
            
        } catch (SQLException e) {
            Core.getLogger().severe("[MemberData] Erro ao criar tabela '" + tableName + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Member> listAll(UUID id) {
        List<Member> members = new ArrayList<>();

        for (String tableName : MEMBER_TABLE_NAMES) {
            try {
                String sql = "SELECT data FROM " + tableName + " WHERE id = ?";
                
                try (Connection conn = Core.getPostgres().getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    
                    stmt.setString(1, id.toString());
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        String jsonData = rs.getString("data");
                        try {
                            members.add(Core.GSON.fromJson(jsonData, Member.class));
                        } catch (Throwable t) {
                            Core.getLogger().severe("[MemberData] listAll: skip member na tabela " + tableName + ": " + t);
                        }
                    } else {
                        System.out.println("Não encontrou o usuário esperado da tabela " + tableName + "!");
                    }
                }

            } catch (Exception e) {
                System.err.println("Erro ao acessar a tabela " + tableName + "!");
                e.printStackTrace();
            }
        }

        return members;
    }

    /**
     * Evita PlayerJoin bloqueando se Gson falhar (JSON estragado ou JAR incompleto/corrupto nos classes do member).
     */
    @SuppressWarnings("unchecked")
    private T safeFromJson(String jsonData, Type gsonType) {
        if (jsonData == null || jsonData.trim().isEmpty())
            return null;
        try {
            return (T) Core.GSON.fromJson(jsonData, gsonType);
        } catch (Throwable t) {
            Core.getLogger().severe("[MemberData] Gson falhou (tabela " + tableName + ", tipo "
                    + generic.getSimpleName() + "): " + t);
            if (t.getCause() != null || !(t instanceof com.google.gson.JsonSyntaxException))
                t.printStackTrace();
            return null;
        }
    }

    public T save(T member) {
        String checkSql = "SELECT data FROM " + tableName + " WHERE id = ?";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            
            stmt.setString(1, member.getId().toString());
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                String insertSql = "INSERT INTO " + tableName + " (id, account_id, data) VALUES (?, ?, ?::jsonb)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, member.getId().toString());
                    insertStmt.setString(2, member.getId().toString());
                    insertStmt.setString(3, Core.GSON.toJson(member));
                    insertStmt.executeUpdate();
                }
                
                controller.save(member);
            } else {
                String jsonData = rs.getString("data");
                T parsed = safeFromJson(jsonData, type);
                if (parsed != null)
                    member = parsed;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return member;
    }

    public T of(UUID id, boolean storeInRedis) {
        Member member = controller.of(id, generic);

        if (member == null) {
            // Carregar direto do PostgreSQL
            String sql = "SELECT data FROM " + tableName + " WHERE id = ?";
            
            try (Connection conn = postgres.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, id.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String jsonData = rs.getString("data");
                    T parsed = safeFromJson(jsonData, generic);
                    if (parsed != null)
                        member = parsed;
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return generic.cast(member);
    }

    public T of(UUID id) {
        return of(id, false);
    }

    public synchronized void update(T member, String field) {
        try {
            String sql = "UPDATE " + tableName + " SET data = ?::jsonb WHERE id = ?";
            
            try (Connection conn = postgres.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, Core.GSON.toJson(member));
                stmt.setString(2, member.getId().toString());
                stmt.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Core.getLogger().severe("[Member-Update] Não foi possível atualizar o campo \"" + field + "\" no membro de " + member.getName() + "!");
        }
    }

    public void startExpiration(UUID id) {
        controller.remove(id, generic);

        redis.cache(MEMBER_KEY_FORMAT + id, 300);
    }

    public void cancelExpiration(UUID id) {
        redis.persist(MEMBER_KEY_FORMAT + id);
    }

    public List<T> list() {
        List<T> members = new ArrayList<>();
        String sql = "SELECT data FROM " + tableName;
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String jsonData = rs.getString("data");
                T member = safeFromJson(jsonData, generic);
                if (member != null)
                    members.add(member);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return members;
    }

    public Collection<T> ranking(String field, int limit) {
        List<T> members = new ArrayList<>();
        String sql = "SELECT data FROM " + tableName + " ORDER BY (data->>?)::numeric DESC LIMIT ?";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, field);
            stmt.setInt(2, limit * 2);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String jsonData = rs.getString("data");
                T member = safeFromJson(jsonData, generic);
                if (member != null)
                    members.add(member);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return members;
    }
}
