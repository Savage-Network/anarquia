package com.minecraft.game.anticheat;

import com.minecraft.game.anticheat.checks.BlockCheckListener;
import com.minecraft.game.anticheat.checks.CombatCheckListener;
import com.minecraft.game.anticheat.checks.MiscCheckListener;
import com.minecraft.game.anticheat.checks.MovementCheckListener;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.bukkit.vanish.VanishService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Motor central do anticheat. */
public final class AnticheatEngine {

    private static final Map<String, Integer> CHECK_VIOLATIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> TOTAL_VIOLATIONS = new ConcurrentHashMap<>();
    private static final Map<String, Long> ALERT_COOLDOWN = new ConcurrentHashMap<>();
    private static final Map<String, Deque<AnticheatFlagRecord>> FLAG_HISTORY = new ConcurrentHashMap<>();

    private static final int MAX_FLAG_HISTORY = 8;
    private static final long ALERT_COOLDOWN_MS = 1_800L;

    private static GamePlugin plugin;

    private AnticheatEngine() {}

    public static void start(GamePlugin gamePlugin) {
        plugin = gamePlugin;
        Bukkit.getPluginManager().registerEvents(new MovementCheckListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new CombatCheckListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new BlockCheckListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new MiscCheckListener(), plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, MiscCheckListener::flushCpsWindow, 20L, 20L);
    }

    public static void reload() {
        // listeners stay registered; config hot-reloads
    }

    public static void flag(Player player, AnticheatCheck check, String detail) {
        if (player == null || check == null) return;
        if (!AnticheatConfig.isEnabled() || !AnticheatConfig.isCheckEnabled(check)) return;
        if (isExempt(player)) return;

        UUID id = player.getUniqueId();
        String cdKey = id + ":" + check.name();
        long now = System.currentTimeMillis();
        Long last = ALERT_COOLDOWN.get(cdKey);
        if (last != null && now - last < ALERT_COOLDOWN_MS) return;
        ALERT_COOLDOWN.put(cdKey, now);

        int checkVl = incrementCheckViolation(id, check);
        int totalVl = incrementTotalViolation(id);
        recordFlag(id, check, detail, checkVl, totalVl);
        AnticheatAlertService.flag(player, check.getDisplayName(), detail, checkVl, totalVl);
        AnticheatPunishService.evaluate(player, totalVl);
    }

    public static int getTotalViolations(UUID id) {
        return TOTAL_VIOLATIONS.getOrDefault(id, 0);
    }

    public static int getCheckViolations(UUID id, AnticheatCheck check) {
        return CHECK_VIOLATIONS.getOrDefault(violationKey(id, check), 0);
    }

    public static List<AnticheatFlagRecord> getFlagHistory(UUID id, AnticheatCheck check) {
        Deque<AnticheatFlagRecord> deque = FLAG_HISTORY.get(historyKey(id, check));
        if (deque == null || deque.isEmpty()) return List.of();
        return List.copyOf(deque);
    }

    public static AnticheatFlagRecord getLastFlag(UUID id, AnticheatCheck check) {
        Deque<AnticheatFlagRecord> deque = FLAG_HISTORY.get(historyKey(id, check));
        return deque == null || deque.isEmpty() ? null : deque.peekFirst();
    }

    public static long getAlertCooldownRemainingMs(UUID id, AnticheatCheck check) {
        Long last = ALERT_COOLDOWN.get(violationKey(id, check));
        if (last == null) return 0L;
        long rem = ALERT_COOLDOWN_MS - (System.currentTimeMillis() - last);
        return Math.max(0L, rem);
    }

    public static int countChecksWithViolations(UUID id) {
        int n = 0;
        for (AnticheatCheck c : AnticheatCheck.values()) {
            if (getCheckViolations(id, c) > 0) n++;
        }
        return n;
    }

    public static int getCategoryViolations(UUID id, AnticheatCheck.Category category) {
        int sum = 0;
        for (AnticheatCheck c : AnticheatCheck.values()) {
            if (c.getCategory() == category) sum += getCheckViolations(id, c);
        }
        return sum;
    }

    /** Motivos pelos quais o jogador está isento de checks no momento. */
    public static List<String> exemptReasons(Player player) {
        if (player == null) return List.of("Jogador offline");
        List<String> reasons = new ArrayList<>();
        if (AnticheatConfig.isSessionExempt(player.getUniqueId())) {
            reasons.add("Isento na sessão (/ac jogadores)");
        }
        if (VanishService.isVanished(player)) reasons.add("Modo vanish");
        GameMode mode = player.getGameMode();
        if (mode == GameMode.CREATIVE) reasons.add("Gamemode Creative");
        if (mode == GameMode.SPECTATOR) reasons.add("Gamemode Spectator");
        if (player.isFlying() || player.getAllowFlight()) reasons.add("Voando");
        if (player.isInsideVehicle()) reasons.add("Dentro de veículo");
        if (player.isGliding()) reasons.add("Planando (elytra)");
        if (player.isRiptiding()) reasons.add("Riptide");
        if (player.hasPermission("game.anticheat.bypass")) reasons.add("Perm game.anticheat.bypass");
        return reasons;
    }

    public static void resetViolations(UUID id) {
        if (id == null) return;
        TOTAL_VIOLATIONS.remove(id);
        CHECK_VIOLATIONS.keySet().removeIf(k -> k.startsWith(id + ":"));
        ALERT_COOLDOWN.keySet().removeIf(k -> k.startsWith(id + ":"));
        FLAG_HISTORY.keySet().removeIf(k -> k.startsWith(id + ":"));
    }

    /** Zera VL e histórico de um check específico. */
    public static void resetCheckViolations(UUID id, AnticheatCheck check) {
        if (id == null || check == null) return;
        String key = violationKey(id, check);
        int removed = CHECK_VIOLATIONS.getOrDefault(key, 0);
        CHECK_VIOLATIONS.remove(key);
        ALERT_COOLDOWN.remove(key);
        FLAG_HISTORY.remove(historyKey(id, check));
        if (removed > 0) {
            TOTAL_VIOLATIONS.merge(id, -removed, (a, b) -> Math.max(0, a + b));
        }
    }

    public static void clear(UUID id) {
        resetViolations(id);
        AnticheatProfiles.remove(id);
    }

    public static boolean isExempt(Player player) {
        if (player == null) return true;
        if (AnticheatConfig.isSessionExempt(player.getUniqueId())) return true;
        GameMode mode = player.getGameMode();
        if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) return true;
        if (player.isFlying() || player.getAllowFlight()) return true;
        if (player.isInsideVehicle()) return true;
        if (player.isGliding() || player.isRiptiding()) return true;
        if (VanishService.isVanished(player)) return true;
        return player.hasPermission("game.anticheat.bypass");
    }

    private static int incrementCheckViolation(UUID id, AnticheatCheck check) {
        String k = violationKey(id, check);
        int next = CHECK_VIOLATIONS.getOrDefault(k, 0) + 1;
        CHECK_VIOLATIONS.put(k, next);
        return next;
    }

    private static int incrementTotalViolation(UUID id) {
        int next = TOTAL_VIOLATIONS.getOrDefault(id, 0) + 1;
        TOTAL_VIOLATIONS.put(id, next);
        return next;
    }

    private static String violationKey(UUID id, AnticheatCheck check) {
        return id + ":" + check.name();
    }

    private static String historyKey(UUID id, AnticheatCheck check) {
        return id + ":" + check.name();
    }

    private static void recordFlag(UUID id, AnticheatCheck check, String detail,
                                   int checkVl, int totalVl) {
        String key = historyKey(id, check);
        Deque<AnticheatFlagRecord> deque = FLAG_HISTORY.computeIfAbsent(key, k -> new ArrayDeque<>());
        deque.addFirst(new AnticheatFlagRecord(check, detail == null ? "" : detail, System.currentTimeMillis(),
                checkVl, totalVl));
        while (deque.size() > MAX_FLAG_HISTORY) {
            deque.removeLast();
        }
    }
}
