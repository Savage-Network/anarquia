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
    public long lastTotemSwapMs;
    public long lastArmorSwapMs;
    public int placesThisSecond;
    public int breaksThisSecond;
    public long placeWindowStartMs;
    public long breakWindowStartMs;
    public int movesThisSecond;
    public long moveWindowStartMs;
    public int lastCpsLeft;
    public int lastCpsRight;
    public Location lastLocation;
    public Location lastValidLocation;
    public boolean wasOnGround;
    public float lastYaw;
    public float lastPitch;
    public long lastActionMs;
    public int lastActionType;
    public long lastHitTick;
    public int hitsThisTick;

    private static final int ACTION_ATTACK = 1;
    private static final int ACTION_BREAK = 2;
    private static final int ACTION_PLACE = 3;

    private final Map<UUID, Long> targetHits = new ConcurrentHashMap<>();
    private final Map<AnticheatCheck, Integer> violationBuffers = new ConcurrentHashMap<>();

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
        long tick = lastHitMs / 50;
        if (tick == lastHitTick) hitsThisTick++;
        else {
            lastHitTick = tick;
            hitsThisTick = 1;
        }
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

    public int incrementBuffer(AnticheatCheck check) {
        return violationBuffers.merge(check, 1, Integer::sum);
    }

    public void resetBuffer(AnticheatCheck check) {
        violationBuffers.remove(check);
    }

    public int bufferOf(AnticheatCheck check) {
        return violationBuffers.getOrDefault(check, 0);
    }

    public void recordAction(int type) {
        long now = System.currentTimeMillis();
        if (now - lastActionMs < 50 && lastActionType != 0 && lastActionType != type) {
            lastActionMs = now;
            lastActionType = type | lastActionType;
        } else {
            lastActionType = type;
            lastActionMs = now;
        }
    }

    public boolean hasMultiActionConflict() {
        int mask = lastActionType;
        int count = 0;
        if ((mask & ACTION_ATTACK) != 0) count++;
        if ((mask & ACTION_BREAK) != 0) count++;
        if ((mask & ACTION_PLACE) != 0) count++;
        return count >= 2 && System.currentTimeMillis() - lastActionMs < 100;
    }

    void tickPlaceWindow() {
        long now = System.currentTimeMillis();
        if (now - placeWindowStartMs >= 1000L) {
            placeWindowStartMs = now;
            placesThisSecond = 0;
        }
    }

    void tickBreakWindow() {
        long now = System.currentTimeMillis();
        if (now - breakWindowStartMs >= 1000L) {
            breakWindowStartMs = now;
            breaksThisSecond = 0;
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
        recordAction(ACTION_PLACE);
    }

    public void incrementBreaks() {
        tickBreakWindow();
        breaksThisSecond++;
        recordAction(ACTION_BREAK);
    }

    public void incrementMoves() {
        tickMoveWindow();
        movesThisSecond++;
    }

    public void recordAttack() {
        recordAction(ACTION_ATTACK);
    }

    public void setCps(int left, int right) {
        lastCpsLeft = left;
        lastCpsRight = right;
    }

    int maxCps() {
        return Math.max(lastCpsLeft, lastCpsRight);
    }
}
