package com.minecraft.core.backend.data.list.api;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.backend.database.postgresql.PostgresDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PunishmentData {

    private final PostgresDatabase postgres;

    public PunishmentData(PostgresDatabase postgres) {
        this.postgres = postgres;
        
        // Criar tabela se não existir
        createTable(postgres);
    }
    
    /**
     * Cria a tabela de punishments no PostgreSQL se ela não existir
     */
    public static void createTable(PostgresDatabase postgres) {
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS punishments (
                    id VARCHAR(255) PRIMARY KEY,
                    player UUID NOT NULL,
                    author UUID NOT NULL,
                    data JSONB NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                
                CREATE INDEX IF NOT EXISTS idx_punishments_player ON punishments(player);
                CREATE INDEX IF NOT EXISTS idx_punishments_author ON punishments(author);
                """)) {
            
            stmt.execute();
            Core.getLogger().info("[PunishmentData] Tabela 'punishments' verificada/criada com sucesso.");
            
        } catch (SQLException e) {
            Core.getLogger().severe("[PunishmentData] Erro ao criar tabela 'punishments': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void save(Punishment punishment) {
        String checkSql = "SELECT id FROM punishments WHERE id = ?";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            
            stmt.setString(1, punishment.getId());
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                String insertSql = "INSERT INTO punishments (id, player, author, data) VALUES (?, ?, ?, ?::jsonb)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, punishment.getId());
                    insertStmt.setObject(2, punishment.getPlayer());
                    insertStmt.setObject(3, punishment.getAuthor());
                    insertStmt.setString(4, Core.GSON.toJson(punishment));
                    insertStmt.executeUpdate();
                }
                
                // Adicionar punição apenas ao histórico do jogador alvo, não ao autor
                Account player = Core.getAccountData().of(punishment.getPlayer());
                
                if (player != null && !player.getId().equals(punishment.getAuthor()))
                    player.addPunishmentToHistory(punishment);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Punishment punishment) {
        String sql = "DELETE FROM punishments WHERE id = ?";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, punishment.getId());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void update(Punishment punishment, String field) {
        String sql = "UPDATE punishments SET data = ?::jsonb WHERE id = ?";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, Core.GSON.toJson(punishment));
            stmt.setString(2, punishment.getId());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean has(UUID player, PunishmentCategory category) {
        return of(player, category) != null;
    }

    public Punishment of(String ip, PunishmentCategory category) {
        Punishment punishment = listAssets(category).stream()
                .filter(search -> search.getIp() != null && search.getIp().equalsIgnoreCase(ip) && search.isValid())
                .findFirst()
                .orElse(null);

        if (punishment != null && punishment.hasExpired()) {
            punishment.revoke(Constant.DEFAULT_ID, "Expirado");

            Account target = Core.getAccountData().of(punishment.getPlayer(), false);

            if (target != null)
                target.updatePunishment(punishment);

            return null;
        }

        return punishment;
    }

    public Punishment of(UUID player, PunishmentCategory category) {
        return listAssets(category).stream()
                .filter(punishment -> punishment.getPlayer().equals(player) && punishment.isValid())
                .findFirst()
                .orElse(null);
    }

    public Punishment of(String id) {
        return list().stream()
                .filter(punishment -> punishment.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public List<Punishment> listAssets(PunishmentCategory category) {
        return list(category).stream().filter(punishment -> punishment.isValid() && !punishment.getRevocation().isValid()).collect(Collectors.toList());
    }

    public List<Punishment> list() {
        List<Punishment> punishments = new ArrayList<>();
        String sql = "SELECT data FROM punishments";
        
        try (Connection conn = postgres.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String jsonData = rs.getString("data");
                Punishment punishment = Core.GSON.fromJson(jsonData, Punishment.class);
                punishments.add(punishment);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return punishments;
    }

    public List<Punishment> list(PunishmentCategory category) {
        return list().stream().filter(punishment -> punishment.getCategory().equals(category)).collect(Collectors.toList());
    }
}
