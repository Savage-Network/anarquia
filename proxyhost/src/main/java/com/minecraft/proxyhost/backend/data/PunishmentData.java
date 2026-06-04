package com.minecraft.proxyhost.backend.data;

import com.minecraft.proxyhost.Constant;
import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.api.punishment.Punishment;
import com.minecraft.proxyhost.api.punishment.PunishmentCategory;
import com.minecraft.proxyhost.backend.database.HikariProvider;
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
public final class PunishmentData {

    public static final String TABLE = "punishments";

    private final HikariProvider database;

    public void createTables() {
        String sql = """
                CREATE TABLE IF NOT EXISTS punishments (
                    id          VARCHAR(32) PRIMARY KEY,
                    player      VARCHAR(36) NOT NULL,
                    author      VARCHAR(36) NOT NULL,
                    data        TEXT NOT NULL,
                    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar tabela " + TABLE, e);
        }
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "CREATE INDEX IF NOT EXISTS idx_punishments_player ON punishments(player)")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public void save(Punishment punishment) {
        String check = "SELECT id FROM punishments WHERE id = ?";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(check)) {
            ps.setString(1, punishment.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    update(punishment);
                    return;
                }
            }
            String insert = "INSERT INTO punishments (id, player, author, data) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ins = c.prepareStatement(insert)) {
                ins.setString(1, punishment.getId());
                ins.setString(2, punishment.getPlayer().toString());
                ins.setString(3, punishment.getAuthor().toString());
                ins.setString(4, Core.GSON.toJson(punishment));
                ins.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao salvar punição " + punishment.getId(), e);
        }
    }

    public void update(Punishment punishment) {
        String sql = "UPDATE punishments SET data = ? WHERE id = ?";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Core.GSON.toJson(punishment));
            ps.setString(2, punishment.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao atualizar punição " + punishment.getId(), e);
        }
    }

    public Optional<Punishment> findById(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        String sql = "SELECT data FROM punishments WHERE LOWER(id) = ?";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(fromJson(rs.getString("data")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar punição " + id, e);
        }
        return Optional.empty();
    }

    public Optional<Punishment> findActive(UUID player, PunishmentCategory category) {
        String sql = "SELECT data FROM punishments WHERE player = ?";
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Punishment p = fromJson(rs.getString("data"));
                    if (p.getCategory() != category) continue;
                    if (!p.isActive()) continue;
                    if (p.hasExpired()) {
                        p.revoke(Constant.DEFAULT_ID, "Expirado");
                        update(p);
                        continue;
                    }
                    return Optional.of(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar punição ativa", e);
        }
        return Optional.empty();
    }

    public List<Punishment> listByPlayer(UUID player, int limit) {
        String sql = "SELECT data FROM punishments WHERE player = ? ORDER BY created_at DESC LIMIT ?";
        List<Punishment> list = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(fromJson(rs.getString("data")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar punições", e);
        }
        return list;
    }

    private static Punishment fromJson(String json) {
        Punishment p = Core.GSON.fromJson(json, Punishment.class);
        if (p.getRevocation() == null) p.setRevocation(new com.minecraft.proxyhost.api.punishment.Revocation());
        return p;
    }
}
