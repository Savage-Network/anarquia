package com.minecraft.game.anticheat;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Estado por jogador usado pelos checks. */
public final class AnticheatProfile {

    private final UUID playerId;

    public int airTicks;
    public float lastFallDistance;
    public long lastSwingMs;
    public long lastHitMs;
    public long lastBreakStartMs;
    public long lastEatStartMs;
    public long lastBowDrawMs;
    public long lastPlaceMs;
    public int placesThisSecond;
    public long placeWindowStartMs;
    public int movesThisSecond;
    public long moveWindowStartMs;
    public int lastCpsLeft;
    public int lastCpsRight;
    public Location lastLocation;
    public boolean wasOnGround;

    private final Map<UUID, Long> targetHits = new ConcurrentHashMap<>();

    AnticheatProfile(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void recordSwing() {
        lastSwingMs = System.currentTimeMillis();
    }

    public void recordHit(Entity target) {
        lastHitMs = System.currentTimeMillis();
        if (target == null) return;
        long now = System.currentTimeMillis();
        long window = AnticheatConfig.numInt("kill-aura.window-ms", 500);
        targetHits.put(target.getUniqueId(), now);
        targetHits.entrySet().removeIf(e -> now - e.getValue() > window);
    }

    public int distinctTargetsInWindow() {
        return targetHits.size();
    }

    public void clearTargetWindow() {
        targetHits.clear();
    }

    void tickPlaceWindow() {
        long now = System.currentTimeMillis();
        if (now - placeWindowStartMs >= 1000L) {
            placeWindowStartMs = now;
            placesThisSecond = 0;
        }
    }

    void tickMoveWindow() {
        long now = System.currentTimeMillis();
        if (now - moveWindowStartMs >= 1000L) {
            moveWindowStartMs = now;
            movesThisSecond = 0;
        }
    }

    public void incrementPlaces() {
        tickPlaceWindow();
        placesThisSecond++;
    }

    public void incrementMoves() {
        tickMoveWindow();
        movesThisSecond++;
    }

    public void setCps(int left, int right) {
        lastCpsLeft = left;
        lastCpsRight = right;
    }

    int maxCps() {
        return Math.max(lastCpsLeft, lastCpsRight);
    }
}
