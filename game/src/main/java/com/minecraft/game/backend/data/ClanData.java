package com.minecraft.game.backend.data;

import com.minecraft.game.Core;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanRequest;
import com.minecraft.game.backend.database.HikariProvider;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public final class ClanData {

    private static final long REQUEST_TTL_MS = 300_000L;

    private final HikariProvider database;
    private final Map<UUID, Clan> cache = new ConcurrentHashMap<>();
    private final Map<String, ClanRequest> requests = new ConcurrentHashMap<>();

    public void createTables() {
        String json = database.getType() == HikariProvider.DatabaseType.MYSQL ? "JSON" : "TEXT";
        String sql = """
                CREATE TABLE IF NOT EXISTS clans (
                    id          VARCHAR(36) PRIMARY KEY,
                    name        VARCHAR(32) NOT NULL,
                    tag         VARCHAR(16) NOT NULL,
                    data        %s         NOT NULL,
                    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""".formatted(json);

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar tabela clans", e);
        }
    }

    public void save(Clan clan) {
        clan.normalizeStyle();
        cache.put(clan.getId(), clan);
        String json = Core.GSON.toJson(clan);
        String upsert = database.getType() == HikariProvider.DatabaseType.MYSQL
                ? """
                INSERT INTO clans (id, name, tag, data) VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE name = VALUES(name), tag = VALUES(tag),
                data = VALUES(data), updated_at = CURRENT_TIMESTAMP"""
                : """
                INSERT INTO clans (id, name, tag, data) VALUES (?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET name = excluded.name, tag = excluded.tag,
                data = excluded.data, updated_at = CURRENT_TIMESTAMP""";

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(upsert)) {
            ps.setString(1, clan.getId().toString());
            ps.setString(2, clan.getName());
            ps.setString(3, clan.getTagWithoutColors());
            ps.setString(4, json);
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao salvar cla: " + e.getMessage());
        }
    }

    public Clan of(UUID id) {
        if (id == null) return null;
        Clan cached = cache.get(id);
        if (cached != null) return cached;

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT data FROM clans WHERE id = ?")) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Clan clan = decodeClan(rs.getString("data"));
                if (clan != null) cache.put(id, clan);
                return clan;
            }
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao carregar cla " + id + ": " + e.getMessage());
            return null;
        }
    }

    public Clan getByName(String name) {
        Optional<Clan> inCache = cache.values().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst();
        if (inCache.isPresent()) return inCache.get();

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT data FROM clans WHERE LOWER(name) = LOWER(?)")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Clan clan = decodeClan(rs.getString("data"));
                if (clan != null) cache.put(clan.getId(), clan);
                return clan;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    /** Resolve cla pelo UUID do membro (repara metadata desatualizada). */
    public Clan findByMember(UUID playerId) {
        if (playerId == null) return null;
        for (Clan clan : listAll()) {
            if (clan != null && clan.isMember(playerId)) return clan;
        }
        return null;
    }

    public Clan getByTag(String tag) {
        String clean = tag == null ? "" : tag.replaceAll("§.", "").replaceAll("&.", "");
        Optional<Clan> inCache = cache.values().stream()
                .filter(c -> c.getTagWithoutColors().equalsIgnoreCase(clean))
                .findFirst();
        if (inCache.isPresent()) return inCache.get();

        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT data FROM clans WHERE LOWER(tag) = LOWER(?)")) {
            ps.setString(1, clean);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Clan clan = decodeClan(rs.getString("data"));
                if (clan != null) cache.put(clan.getId(), clan);
                return clan;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public void delete(Clan clan) {
        cache.remove(clan.getId());
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM clans WHERE id = ?")) {
            ps.setString(1, clan.getId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            Core.getLogger().severe("Erro ao deletar cla: " + e.getMessage());
        }
    }

    public List<Clan> listTop(int limit) {
        return listAll().stream()
                .sorted((a, b) -> Integer.compare(b.getPower(), a.getPower()))
                .limit(limit)
                .toList();
    }

    public List<Clan> listAll() {
        if (!cache.isEmpty()) return new ArrayList<>(cache.values());
        List<Clan> all = new ArrayList<>();
        try (Connection c = database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT data FROM clans")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Clan clan = decodeClan(rs.getString("data"));
                    if (clan != null) {
                        cache.put(clan.getId(), clan);
                        all.add(clan);
                    }
                }
            }
        } catch (SQLException ignored) {
        }
        return all;
    }

    public List<Clan> search(String query, int limit) {
        if (query == null || query.isBlank()) {
            return listAll().stream().limit(limit).toList();
        }
        String q = query.toLowerCase();
        return listAll().stream()
                .filter(c -> c.getName().toLowerCase().contains(q)
                        || c.getTagWithoutColors().toLowerCase().contains(q))
                .limit(limit)
                .toList();
    }

    /* ----- convites ----- */

    public void purgeExpiredRequests() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, ClanRequest>> it = requests.entrySet().iterator();
        while (it.hasNext()) {
            if (now - it.next().getValue().getSentAt() > REQUEST_TTL_MS) it.remove();
        }
    }

    public boolean hasRequest(Clan clan, UUID receiver) {
        purgeExpiredRequests();
        return requests.values().stream()
                .anyMatch(r -> r.getClanId().equals(clan.getId()) && r.getReceiver().equals(receiver));
    }

    public ClanRequest getRequest(UUID author, UUID receiver) {
        purgeExpiredRequests();
        return requests.values().stream()
                .filter(r -> r.getAuthor().equals(author) && r.getReceiver().equals(receiver))
                .findFirst()
                .orElse(null);
    }

    public List<ClanRequest> getPendingInvites(UUID receiver) {
        purgeExpiredRequests();
        List<ClanRequest> out = new ArrayList<>();
        for (ClanRequest r : requests.values()) {
            if (r.getReceiver().equals(receiver)) out.add(r);
        }
        return out;
    }

    public ClanRequest getRequestByClan(UUID receiver, UUID clanId) {
        purgeExpiredRequests();
        return requests.values().stream()
                .filter(r -> r.getReceiver().equals(receiver) && r.getClanId().equals(clanId))
                .findFirst()
                .orElse(null);
    }

    public void removeRequestsForReceiver(UUID receiver) {
        requests.entrySet().removeIf(e -> e.getValue().getReceiver().equals(receiver));
    }

    public void addRequest(ClanRequest request) {
        requests.put(request.key(), request);
    }

    public void removeRequest(ClanRequest request) {
        requests.remove(request.key());
    }

    private static Clan decodeClan(String json) {
        if (json == null || json.isBlank()) return null;
        Clan clan = Core.GSON.fromJson(json, Clan.class);
        if (clan != null) clan.normalizeStyle();
        return clan;
    }
}
