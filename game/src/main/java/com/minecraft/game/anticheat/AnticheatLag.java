package com.minecraft.game.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** Compensação de lag/TPS para reduzir falsos positivos. */
public final class AnticheatLag {

    private static double lastTps = 20.0;
    private static long lastTpsSampleMs;

    private AnticheatLag() {}

    public static void sampleTps() {
        lastTps = Bukkit.getTPS()[0];
        lastTpsSampleMs = System.currentTimeMillis();
    }

    public static double tps() {
        if (System.currentTimeMillis() - lastTpsSampleMs > 5000L) {
            sampleTps();
        }
        return lastTps;
    }

    public static int ping(Player player) {
        if (player == null) return 0;
        return player.getPing();
    }

    /** Multiplicador de tolerância: TPS baixo ou ping alto = limites mais permissivos. */
    public static double toleranceMultiplier(Player player) {
        if (!AnticheatConfig.isLagCompensation()) return 1.0;
        double tpsFactor = 1.0;
        double t = tps();
        if (t < 18.0) tpsFactor += (18.0 - t) * 0.08;
        if (t < 15.0) tpsFactor += 0.15;

        int ping = ping(player);
        double pingFactor = 1.0;
        if (ping > 120) pingFactor += (ping - 120) * 0.0015;
        if (ping > 250) pingFactor += 0.12;

        return Math.min(2.0, tpsFactor * pingFactor);
    }

    public static double adjustLimit(double base, Player player) {
        return base * toleranceMultiplier(player);
    }

    public static int adjustBuffer(int base, Player player) {
        if (!AnticheatConfig.isLagCompensation()) return base;
        int extra = 0;
        if (tps() < 17.0) extra++;
        if (ping(player) > 180) extra++;
        return base + extra;
    }
}
