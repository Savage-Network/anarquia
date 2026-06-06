package com.minecraft.eventos.engine;

/** Regras de gameplay de um modo. */
public record EventModeRules(
        boolean damage,
        boolean food,
        boolean dropItems,
        boolean blockPlace,
        boolean blockBreak,
        boolean buckets,
        boolean friendlyFire,
        boolean respawn
) {
    public static EventModeRules pvpArena() {
        return new EventModeRules(true, false, false, false, false, false, false, false);
    }

    public static EventModeRules spleef() {
        return new EventModeRules(false, false, false, false, true, false, false, false);
    }

    public static EventModeRules ffa() {
        return new EventModeRules(true, false, false, false, false, false, true, true);
    }
}
