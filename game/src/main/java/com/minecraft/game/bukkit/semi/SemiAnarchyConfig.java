package com.minecraft.game.bukkit.semi;

import com.minecraft.game.GamePlugin;
import com.minecraft.game.GameSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SemiAnarchyConfig {

    private static int spawnRadius = 50;
    private static int combatTagSeconds = 15;
    private static Location spawnPoint;
    private static int killRewardBase = 200;
    private static int killStreakBonus = 50;
    private static int maxKillStreakBonus = 200;
    private static int startingBalance = 1000;
    private static String currencySymbol = "$";
    private static int homeCooldownBaseMs = 30_000;
    private static int homeCooldownVipMs = 15_000;
    private static int spawnCooldownMs = 30_000;
    private static int warpCooldownMs = 5_000;
    private static int bountyMin = 500;
    private static int bountyTaxPercent = 10;
    private static Map<String, Location> warps = new LinkedHashMap<>();

    private static String pendingSpawnWorld = "world";
    private static double pendingSpawnX = 0.5;
    private static double pendingSpawnY = 64.0;
    private static double pendingSpawnZ = 0.5;
    private static float pendingSpawnYaw;
    private static float pendingSpawnPitch;

    private SemiAnarchyConfig() {}

    public static void load(GamePlugin plugin) {
        spawnRadius = GameSettings.SPAWN_PROTECTION_RADIUS;
        combatTagSeconds = GameSettings.COMBAT_TAG_SECONDS;
        startingBalance = GameSettings.STARTING_BALANCE;
        currencySymbol = GameSettings.CURRENCY_SYMBOL;
        killRewardBase = GameSettings.KILL_REWARD_BASE;
        killStreakBonus = GameSettings.KILL_STREAK_BONUS;
        maxKillStreakBonus = GameSettings.MAX_KILL_STREAK_BONUS;
        homeCooldownBaseMs = GameSettings.HOME_COOLDOWN_SECONDS * 1000;
        homeCooldownVipMs = GameSettings.HOME_COOLDOWN_VIP_SECONDS * 1000;
        spawnCooldownMs = GameSettings.SPAWN_COOLDOWN_SECONDS * 1000;
        warpCooldownMs = GameSettings.WARP_COOLDOWN_SECONDS * 1000;
        bountyMin = GameSettings.BOUNTY_MIN;
        bountyTaxPercent = GameSettings.BOUNTY_TAX_PERCENT;
        pendingSpawnWorld = GameSettings.SPAWN_WORLD;
        pendingSpawnX = GameSettings.SPAWN_X;
        pendingSpawnY = GameSettings.SPAWN_Y;
        pendingSpawnZ = GameSettings.SPAWN_Z;
        pendingSpawnYaw = GameSettings.SPAWN_YAW;
        pendingSpawnPitch = GameSettings.SPAWN_PITCH;
        spawnPoint = null;

        resolveSpawnPoint();
        if (spawnPoint == null) {
            plugin.getServer().getScheduler().runTask(plugin, SemiAnarchyConfig::resolveSpawnPoint);
        }

        loadWarps(plugin);
        if (warps.isEmpty()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> loadWarps(plugin));
        }
    }

    /** Resolve spawn quando o mundo {@link GameSettings#SPAWN_WORLD} estiver carregado. */
    private static void resolveSpawnPoint() {
        World world = Bukkit.getWorld(pendingSpawnWorld);
        if (world == null) {
            spawnPoint = null;
            return;
        }
        spawnPoint = new Location(world, pendingSpawnX, pendingSpawnY, pendingSpawnZ,
                pendingSpawnYaw, pendingSpawnPitch);
    }

    /** Chamado apos {@code /mundo load} ou auto-load do mapa spawn. */
    public static void onWorldLoaded(String worldName) {
        if (worldName != null && worldName.equalsIgnoreCase(pendingSpawnWorld)) {
            resolveSpawnPoint();
        }
        GamePlugin plugin = GamePlugin.getInstance();
        if (plugin != null) {
            loadWarps(plugin);
        }
    }

    private static void loadWarps(GamePlugin plugin) {
        warps = new LinkedHashMap<>();
        File file = new File(plugin.getDataFolder(), "warps.yml");
        if (!file.exists()) plugin.saveResource("warps.yml", false);
        FileConfiguration wcfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = wcfg.getConfigurationSection("warps");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(key);
            if (sec == null) continue;
            World world = Bukkit.getWorld(sec.getString("world", "world"));
            if (world == null) continue;
            warps.put(key.toLowerCase(), new Location(world,
                    sec.getDouble("x"), sec.getDouble("y"), sec.getDouble("z"),
                    (float) sec.getDouble("yaw", 0), (float) sec.getDouble("pitch", 0)));
        }
    }

    public static void reload(GamePlugin plugin) {
        plugin.reloadConfig();
        load(plugin);
    }

    public static int spawnRadius() { return spawnRadius; }
    public static int combatTagSeconds() { return combatTagSeconds; }
    public static long combatTagMs() { return combatTagSeconds * 1000L; }
    public static Location spawnPoint() {
        resolveSpawnPoint();
        if (spawnPoint != null) return spawnPoint.clone();

        if (com.minecraft.game.bukkit.world.WorldManager.existsOnDisk(pendingSpawnWorld)
                && !com.minecraft.game.bukkit.world.WorldManager.isLoaded(pendingSpawnWorld)) {
            com.minecraft.game.bukkit.world.WorldManager.load(pendingSpawnWorld);
            resolveSpawnPoint();
            if (spawnPoint != null) return spawnPoint.clone();
        }

        World world = Bukkit.getWorld(pendingSpawnWorld);
        if (world != null) {
            return new Location(world, pendingSpawnX, pendingSpawnY, pendingSpawnZ,
                    pendingSpawnYaw, pendingSpawnPitch);
        }
        throw new IllegalStateException("Mundo spawn '" + pendingSpawnWorld
                + "' nao carregado. Use /mundo load " + pendingSpawnWorld);
    }
    public static int killRewardBase() { return killRewardBase; }
    public static int killStreakBonus() { return killStreakBonus; }
    public static int maxKillStreakBonus() { return maxKillStreakBonus; }
    public static int startingBalance() { return startingBalance; }
    public static String currencySymbol() { return currencySymbol; }
    public static int homeCooldownMs(boolean vip) { return vip ? homeCooldownVipMs : homeCooldownBaseMs; }
    public static int spawnCooldownMs() { return spawnCooldownMs; }
    public static int warpCooldownMs() { return warpCooldownMs; }
    public static int bountyMin() { return bountyMin; }
    public static int bountyTaxPercent() { return bountyTaxPercent; }
    public static Map<String, Location> warps() { return Collections.unmodifiableMap(warps); }
    public static Location warp(String name) {
        Location loc = warps.get(name.toLowerCase());
        return loc != null ? loc.clone() : null;
    }
}
