package com.minecraft.game.bukkit.semi;

import java.util.UUID;

/** Estado de combat tag de um jogador. */
public final class CombatTag {

    private final long untilMs;
    private final UUID opponentId;
    private final String opponentName;
    private final long startedMs;

    public CombatTag(long untilMs, UUID opponentId, String opponentName, long startedMs) {
        this.untilMs = untilMs;
        this.opponentId = opponentId;
        this.opponentName = opponentName;
        this.startedMs = startedMs;
    }

    public long untilMs() {
        return untilMs;
    }

    public UUID opponentId() {
        return opponentId;
    }

    public String opponentName() {
        return opponentName;
    }

    public long startedMs() {
        return startedMs;
    }

    public boolean isExpired() {
        return untilMs <= System.currentTimeMillis();
    }
}
