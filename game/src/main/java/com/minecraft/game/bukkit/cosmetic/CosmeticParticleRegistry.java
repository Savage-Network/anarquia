package com.minecraft.game.bukkit.cosmetic;

import com.minecraft.game.account.context.objects.cosmetic.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Cache de jogadores com aura ativa — evita varrer todos os online a cada tick. */
public final class CosmeticParticleRegistry {

    private static final Map<UUID, ParticleEffect> ACTIVE = new ConcurrentHashMap<>();

    private CosmeticParticleRegistry() {}

    public static void refresh(UUID playerId, ParticleEffect effect) {
        if (playerId == null) return;
        if (effect == null) ACTIVE.remove(playerId);
        else ACTIVE.put(playerId, effect);
    }

    public static void clear(UUID playerId) {
        if (playerId != null) ACTIVE.remove(playerId);
    }

    public static Map<UUID, ParticleEffect> snapshot() {
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
