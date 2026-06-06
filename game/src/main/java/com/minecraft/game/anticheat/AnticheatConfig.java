package com.minecraft.game.anticheat;

import com.minecraft.game.GamePlugin;
import com.minecraft.game.config.DiscordConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Configuração persistente do anticheat ({@code plugins/game/anticheat.yml}). */
public final class AnticheatConfig {

    private static File file;
    private static FileConfiguration yaml;

    private static boolean enabled = true;
    private static boolean alertStaff = true;
    private static boolean lagCompensation = true;
    private static boolean setbackEnabled = true;

    private static int bufferDefault = 2;
    private static int bufferMovement = 3;
    private static int bufferCombat = 2;
    private static int bufferWorld = 2;
    private static int bufferPlayer = 2;

    private static boolean vlDecayEnabled = true;
    private static long vlDecayIntervalTicks = 1200L;
    private static int vlDecayAmount = 1;

    private static boolean punishEnabled = true;
    private static int kickVl = 25;
    private static int banVl = 50;
    private static boolean resetVlOnQuit = true;

    private static final Map<AnticheatCheck, Boolean> checkEnabled = new EnumMap<>(AnticheatCheck.class);
    private static final Map<String, Double> numericValues = new ConcurrentHashMap<>();
    private static final Set<UUID> sessionExempt = ConcurrentHashMap.newKeySet();

    private AnticheatConfig() {}

    public static void load(GamePlugin plugin) {
        file = new File(plugin.getDataFolder(), "anticheat.yml");
        if (!file.exists()) {
            plugin.saveResource("anticheat.yml", false);
        }
        yaml = YamlConfiguration.loadConfiguration(file);
        mergeDefaults(plugin);
        readAll();
    }

    private static void mergeDefaults(GamePlugin plugin) {
        try (InputStream in = plugin.getResource("anticheat.yml")) {
            if (in == null) return;
            File tmp = File.createTempFile("ac-default", ".yml");
            Files.copy(in, tmp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(tmp);
            yaml.setDefaults(defaults);
            yaml.options().copyDefaults(true);
            tmp.delete();
        } catch (IOException ignored) {
        }
    }

    private static void readAll() {
        enabled = yaml.getBoolean("enabled", true);
        alertStaff = yaml.getBoolean("alert-staff", true);
        lagCompensation = yaml.getBoolean("lag-compensation", true);
        setbackEnabled = yaml.getBoolean("setback.enabled", true);

        bufferDefault = yaml.getInt("buffers.default", 2);
        bufferMovement = yaml.getInt("buffers.movement", 3);
        bufferCombat = yaml.getInt("buffers.combat", 2);
        bufferWorld = yaml.getInt("buffers.world", 2);
        bufferPlayer = yaml.getInt("buffers.player", 2);

        vlDecayEnabled = yaml.getBoolean("vl-decay.enabled", true);
        vlDecayIntervalTicks = yaml.getLong("vl-decay.interval-ticks", 1200L);
        vlDecayAmount = yaml.getInt("vl-decay.amount", 1);

        punishEnabled = yaml.getBoolean("punishments.enabled", true);
        kickVl = yaml.getInt("punishments.kick-vl", 25);
        banVl = yaml.getInt("punishments.ban-vl", 50);
        resetVlOnQuit = yaml.getBoolean("punishments.reset-vl-on-quit", true);

        for (AnticheatCheck check : AnticheatCheck.values()) {
            checkEnabled.put(check, yaml.getBoolean("checks." + check.getConfigKey() + ".enabled", true));
        }

        numericValues.clear();
        putNum("fly.air-ticks", yaml.getDouble("checks.fly.air-ticks", 32));
        putNum("speed.ground-limit", yaml.getDouble("checks.speed.ground-limit", 0.68));
        putNum("speed.air-limit", yaml.getDouble("checks.speed.air-limit", 0.58));
        putNum("speed.sprint-bonus", yaml.getDouble("checks.speed.sprint-bonus", 0.08));
        putNum("no-fall.min-fall", yaml.getDouble("checks.no-fall.min-fall", 4.0));
        putNum("high-jump.max-delta-y", yaml.getDouble("checks.high-jump.max-delta-y", 0.85));
        putNum("step.max-step", yaml.getDouble("checks.step.max-step", 1.25));
        putNum("elytra.max-speed", yaml.getDouble("checks.elytra.max-speed", 1.85));
        putNum("blink.max-delta", yaml.getDouble("checks.blink.max-delta", 9.0));
        putNum("no-slow.max-speed", yaml.getDouble("checks.no-slow.max-speed", 0.22));
        putNum("strafe.min-speed", yaml.getDouble("checks.strafe.min-speed", 0.35));
        putNum("strafe.max-yaw-delta", yaml.getDouble("checks.strafe.max-yaw-delta", 75));
        putNum("reach.max-reach", yaml.getDouble("checks.reach.max-reach", 3.35));
        putNum("hitbox.max-reach", yaml.getDouble("checks.hitbox.max-reach", 3.05));
        putNum("kill-aura.max-targets", yaml.getDouble("checks.kill-aura.max-targets", 3));
        putNum("kill-aura.window-ms", yaml.getDouble("checks.kill-aura.window-ms", 500));
        putNum("auto-click.max-cps", yaml.getDouble("checks.auto-click.max-cps", 22));
        putNum("no-swing.max-ms", yaml.getDouble("checks.no-swing.max-ms", 350));
        putNum("velocity.min-ratio", yaml.getDouble("checks.velocity.min-ratio", 0.35));
        putNum("velocity.delay-ticks", yaml.getDouble("checks.velocity.delay-ticks", 3));
        putNum("aim-assist.max-rotation", yaml.getDouble("checks.aim-assist.max-rotation", 45));
        putNum("multi-hit.max-hits", yaml.getDouble("checks.multi-hit.max-hits", 2));
        putNum("fast-break.min-break-ms", yaml.getDouble("checks.fast-break.min-break-ms", 80));
        putNum("fast-place.max-per-second", yaml.getDouble("checks.fast-place.max-per-second", 14));
        putNum("nuker.max-per-second", yaml.getDouble("checks.nuker.max-per-second", 18));
        putNum("ghost-hand.max-reach", yaml.getDouble("checks.ghost-hand.max-reach", 6.0));
        putNum("fast-eat.min-eat-ms", yaml.getDouble("checks.fast-eat.min-eat-ms", 1400));
        putNum("fast-bow.min-draw-ms", yaml.getDouble("checks.fast-bow.min-draw-ms", 350));
        putNum("timer.max-moves-per-second", yaml.getDouble("checks.timer.max-moves-per-second", 22));
        putNum("inventory-move.speed-limit", yaml.getDouble("checks.inventory-move.speed-limit", 0.45));
        putNum("auto-totem.min-ms", yaml.getDouble("checks.auto-totem.min-ms", 150));
        putNum("auto-armor.min-ms", yaml.getDouble("checks.auto-armor.min-ms", 120));
    }

    public static void save() {
        if (yaml == null || file == null) return;
        yaml.set("enabled", enabled);
        yaml.set("alert-staff", alertStaff);
        yaml.set("lag-compensation", lagCompensation);
        yaml.set("setback.enabled", setbackEnabled);
        yaml.set("buffers.default", bufferDefault);
        yaml.set("buffers.movement", bufferMovement);
        yaml.set("buffers.combat", bufferCombat);
        yaml.set("buffers.world", bufferWorld);
        yaml.set("buffers.player", bufferPlayer);
        yaml.set("vl-decay.enabled", vlDecayEnabled);
        yaml.set("vl-decay.interval-ticks", vlDecayIntervalTicks);
        yaml.set("vl-decay.amount", vlDecayAmount);
        yaml.set("punishments.enabled", punishEnabled);
        yaml.set("punishments.kick-vl", kickVl);
        yaml.set("punishments.ban-vl", banVl);
        yaml.set("punishments.reset-vl-on-quit", resetVlOnQuit);

        for (AnticheatCheck check : AnticheatCheck.values()) {
            yaml.set("checks." + check.getConfigKey() + ".enabled", isCheckEnabled(check));
        }
        for (Map.Entry<String, Double> e : numericValues.entrySet()) {
            yaml.set("checks." + e.getKey(), e.getValue());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            com.minecraft.game.Core.getLogger().warning("Falha ao salvar anticheat.yml: " + e.getMessage());
        }
    }

    public static int bufferSize(AnticheatCheck check) {
        if (check == null) return bufferDefault;
        return switch (check.getCategory()) {
            case MOVEMENT -> bufferMovement;
            case COMBAT -> bufferCombat;
            case WORLD -> bufferWorld;
            case PLAYER -> bufferPlayer;
        };
    }

    private static void putNum(String key, double value) {
        numericValues.put(key, value);
    }

    public static double num(String key, double fallback) {
        return numericValues.getOrDefault(key, fallback);
    }

    public static int numInt(String key, int fallback) {
        return (int) Math.round(num(key, fallback));
    }

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean v) { enabled = v; }

    public static boolean isAlertStaff() { return alertStaff; }
    public static void setAlertStaff(boolean v) { alertStaff = v; }

    public static boolean isLagCompensation() { return lagCompensation; }
    public static void setLagCompensation(boolean v) { lagCompensation = v; }

    public static boolean isSetbackEnabled() { return setbackEnabled; }
    public static void setSetbackEnabled(boolean v) { setbackEnabled = v; }

    public static boolean isVlDecayEnabled() { return vlDecayEnabled; }
    public static long getVlDecayIntervalTicks() { return vlDecayIntervalTicks; }
    public static int getVlDecayAmount() { return vlDecayAmount; }

    public static String getDiscordWebhook() {
        return DiscordConfig.webhook("anticheat");
    }

    public static boolean isPunishEnabled() { return punishEnabled; }
    public static void setPunishEnabled(boolean v) { punishEnabled = v; }

    public static int getKickVl() { return kickVl; }
    public static void setKickVl(int v) { kickVl = Math.max(1, v); }

    public static int getBanVl() { return banVl; }
    public static void setBanVl(int v) { banVl = Math.max(kickVl + 1, v); }

    public static boolean isResetVlOnQuit() { return resetVlOnQuit; }
    public static void setResetVlOnQuit(boolean v) { resetVlOnQuit = v; }

    public static boolean isCheckEnabled(AnticheatCheck check) {
        return checkEnabled.getOrDefault(check, true);
    }

    public static void setCheckEnabled(AnticheatCheck check, boolean enabled) {
        checkEnabled.put(check, enabled);
    }

    public static void toggleCheck(AnticheatCheck check) {
        setCheckEnabled(check, !isCheckEnabled(check));
    }

    public static boolean isSessionExempt(UUID id) {
        return id != null && sessionExempt.contains(id);
    }

    public static void setSessionExempt(UUID id, boolean exempt) {
        if (id == null) return;
        if (exempt) sessionExempt.add(id);
        else sessionExempt.remove(id);
    }

    public static void toggleSessionExempt(UUID id) {
        if (isSessionExempt(id)) sessionExempt.remove(id);
        else sessionExempt.add(id);
    }
}
