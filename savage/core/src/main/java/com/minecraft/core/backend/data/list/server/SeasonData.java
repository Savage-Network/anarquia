package com.minecraft.core.backend.data.list.server;

import com.minecraft.core.Core;
import com.minecraft.core.backend.database.postgresql.PostgresDatabase;
import com.minecraft.core.season.Season;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeasonData {

    private final PostgresDatabase postgres;

    public SeasonData(PostgresDatabase postgres) {
        this.postgres = postgres;
        
        // Criar tabela se não existir
        createTable(postgres);
    }
    
    /**
     * Cria a tabela de seasons no PostgreSQL se ela não existir
     */
    public static void createTable(PostgresDatabase postgres) {
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS seasons (
                    id INT PRIMARY KEY,
                    data JSONB NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """)) {
            
            stmt.execute();
            Core.getLogger().info("[SeasonData] Tabela 'seasons' verificada/criada com sucesso.");
            
        } catch (SQLException e) {
            Core.getLogger().severe("[SeasonData] Erro ao criar tabela 'seasons': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Season getActualSeason() {
        return list().stream().filter(season -> !season.isOver()).findFirst().orElse(null);
    }

    public void save(Season season) {
        String checkSql = "SELECT id FROM seasons WHERE id = ?";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            
            stmt.setInt(1, season.getId());
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                String insertSql = "INSERT INTO seasons (id, data) VALUES (?, ?::jsonb)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, season.getId());
                    insertStmt.setString(2, Core.GSON.toJson(season));
                    insertStmt.executeUpdate();
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void update(Season season, String field) {
        String sql = "UPDATE seasons SET data = ?::jsonb WHERE id = ?";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, Core.GSON.toJson(season));
            stmt.setInt(2, season.getId());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Season of(int id) {
        String sql = "SELECT data FROM seasons WHERE id = ?";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String jsonData = rs.getString("data");
                return Core.GSON.fromJson(jsonData, Season.class);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public int size() {
        String sql = "SELECT COUNT(*) FROM seasons";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    public List<Season> list() {
        List<Season> seasons = new ArrayList<>();
        String sql = "SELECT data FROM seasons";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String jsonData = rs.getString("data");
                Season season = Core.GSON.fromJson(jsonData, Season.class);
                seasons.add(season);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return seasons;
    }
}
