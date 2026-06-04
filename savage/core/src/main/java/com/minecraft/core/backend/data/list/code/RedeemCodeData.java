package com.minecraft.core.backend.data.list.code;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.code.RedeemCode;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.backend.database.postgresql.PostgresDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RedeemCodeData {

    private final PostgresDatabase postgres;

    public RedeemCodeData(PostgresDatabase postgres) {
        this.postgres = postgres;
        
        // Criar tabela se não existir
        createTable(postgres);
    }
    
    /**
     * Cria a tabela de redeem_codes no PostgreSQL se ela não existir
     */
    public static void createTable(PostgresDatabase postgres) {
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS redeem_codes (
                    code VARCHAR(255) PRIMARY KEY,
                    rank_type VARCHAR(50) NOT NULL,
                    duration BIGINT NOT NULL,
                    creator UUID NOT NULL,
                    created_at BIGINT NOT NULL,
                    used BOOLEAN DEFAULT FALSE,
                    used_by UUID,
                    used_at BIGINT
                );
                
                CREATE INDEX IF NOT EXISTS idx_redeem_codes_used ON redeem_codes(used);
                CREATE INDEX IF NOT EXISTS idx_redeem_codes_creator ON redeem_codes(creator);
                """)) {
            
            stmt.execute();
            Core.getLogger().info("[RedeemCodeData] Tabela 'redeem_codes' verificada/criada com sucesso.");
            
        } catch (SQLException e) {
            Core.getLogger().severe("[RedeemCodeData] Erro ao criar tabela 'redeem_codes': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public RedeemCode of(String code) {
        String sql = "SELECT * FROM redeem_codes WHERE UPPER(code) = UPPER(?)";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return fromResultSet(rs);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public void save(RedeemCode redeemCode) {
        String checkSql = "SELECT code FROM redeem_codes WHERE UPPER(code) = UPPER(?)";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            
            stmt.setString(1, redeemCode.getCode());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Update
                String updateSql = "UPDATE redeem_codes SET rank_type = ?, duration = ?, used = ?, used_by = ?, used_at = ? WHERE UPPER(code) = UPPER(?)";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, redeemCode.getRankType().name());
                    updateStmt.setLong(2, redeemCode.getDuration());
                    updateStmt.setBoolean(3, redeemCode.isUsed());
                    updateStmt.setObject(4, redeemCode.getUsedBy());
                    updateStmt.setLong(5, redeemCode.getUsedAt());
                    updateStmt.setString(6, redeemCode.getCode());
                    updateStmt.executeUpdate();
                }
            } else {
                // Insert
                String insertSql = "INSERT INTO redeem_codes (code, rank_type, duration, creator, created_at, used, used_by, used_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, redeemCode.getCode().toUpperCase());
                    insertStmt.setString(2, redeemCode.getRankType().name());
                    insertStmt.setLong(3, redeemCode.getDuration());
                    insertStmt.setObject(4, redeemCode.getCreator());
                    insertStmt.setLong(5, redeemCode.getCreatedAt());
                    insertStmt.setBoolean(6, redeemCode.isUsed());
                    insertStmt.setObject(7, redeemCode.getUsedBy());
                    insertStmt.setLong(8, redeemCode.getUsedAt());
                    insertStmt.executeUpdate();
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<RedeemCode> listAll() {
        List<RedeemCode> codes = new ArrayList<>();
        String sql = "SELECT * FROM redeem_codes ORDER BY created_at DESC";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                codes.add(fromResultSet(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return codes;
    }

    public List<RedeemCode> listUnused() {
        List<RedeemCode> codes = new ArrayList<>();
        String sql = "SELECT * FROM redeem_codes WHERE used = FALSE ORDER BY created_at DESC";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                codes.add(fromResultSet(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return codes;
    }

    private RedeemCode fromResultSet(ResultSet rs) throws SQLException {
        String code = rs.getString("code");
        RankType rankType = RankType.valueOf(rs.getString("rank_type"));
        long duration = rs.getLong("duration");
        UUID creator = (UUID) rs.getObject("creator");
        long createdAt = rs.getLong("created_at");
        boolean used = rs.getBoolean("used");
        
        RedeemCode redeemCode = new RedeemCode(code, rankType, duration, creator);
        redeemCode.setCreatedAt(createdAt);
        redeemCode.setUsed(used);
        
        UUID usedBy = (UUID) rs.getObject("used_by");
        if (usedBy != null) {
            redeemCode.setUsedBy(usedBy);
            redeemCode.setUsedAt(rs.getLong("used_at"));
        }
        
        return redeemCode;
    }
}
