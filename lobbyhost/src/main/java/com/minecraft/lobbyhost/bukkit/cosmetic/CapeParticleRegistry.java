package com.minecraft.lobbyhost.bukkit.cosmetic;

import com.minecraft.lobbyhost.account.context.objects.cosmetic.Cape;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Jogadores com capa de particulas ativa. */
public final class CapeParticleRegistry {

    private static final Map<UUID, Cape> ACTIVE = new ConcurrentHashMap<>();

    private CapeParticleRegistry() {}

    public static void refresh(UUID playerId, Cape cape) {
        if (playerId == null) return;
        if (cape == null) ACTIVE.remove(playerId);
        else ACTIVE.put(playerId, cape);
    }

    public static void clear(UUID playerId) {
        if (playerId != null) ACTIVE.remove(playerId);
    }

    public static Map<UUID, Cape> snapshot() {
        return ACTIVE;
    }

    public static boolean hasNearbyViewer(Player subject, double radiusSq) {
        if (subject == null || !subject.isOnline()) return false;
        var loc = subject.getLocation();
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.getWorld() != subject.getWorld()) continue;
            if (other.equals(subject)) return true;
            if (other.getLocation().distanceSquared(loc) <= radiusSq) return true;
        }
        return false;
    }
}
