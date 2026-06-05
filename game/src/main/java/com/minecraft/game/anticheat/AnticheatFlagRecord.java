package com.minecraft.game.anticheat;

/** Registro de uma detecção do anticheat. */
public record AnticheatFlagRecord(
        AnticheatCheck check,
        String detail,
        long atMs,
        int checkVl,
        int totalVl
) {}
