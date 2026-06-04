package com.minecraft.game.bukkit.semi;

import com.minecraft.game.GamePlugin;
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

    private SemiAnarchyConfig() {}

    public static void load(GamePlugin plugin) {
        FileConfiguration cfg = plugin.getConfig();
        spawnRadius = cfg.getInt("server.spawn-protection-radius", 50);
        combatTagSeconds = cfg.getInt("server.combat-tag-seconds", 15);
        startingBalance = cfg.getInt("economy.starting-balance", 1000);
        currencySymbol = cfg.getString("economy.currency-symbol", "$");

        ConfigurationSection semi = cfg.getConfigurationSection("semi-anarchy");
        if (semi != null) {
            killRewardBase = semi.getInt("kill-reward-base", 200);
            killStreakBonus = semi.getInt("kill-streak-bonus", 50);
            maxKillStreakBonus = semi.getInt("max-kill-streak-bonus", 200);
            homeCooldownBaseMs = semi.getInt("home-cooldown-seconds", 30) * 1000;
            homeCooldownVipMs = semi.getInt("home-cooldown-vip-seconds", 15) * 1000;
            spawnCooldownMs = semi.getInt("spawn-cooldown-seconds", 30) * 1000;
            warpCooldownMs = semi.getInt("warp-cooldown-seconds", 5) * 1000;
            bountyMin = semi.getInt("bounty-min", 500);
            bountyTaxPercent = semi.getInt("bounty-tax-percent", 10);

            ConfigurationSection sp = semi.getConfigurationSection("spawn");
            if (sp != null) {
                World w = Bukkit.getWorld(sp.getString("world", "world"));
                if (w == null) w = Bukkit.getWorlds().getFirst();
                spawnPoint = new Location(w,
                        sp.getDouble("x", w.getSpawnLocation().getX()),
                        sp.getDouble("y", w.getSpawnLocation().getY()),
                        sp.getDouble("z", w.getSpawnLocation().getZ()),
                        (float) sp.getDouble("yaw", 0),
                        (float) sp.getDouble("pitch", 0));
            }
        }

        if (spawnPoint == null) {
            World w = Bukkit.getWorlds().getFirst();
            spawnPoint = w.getSpawnLocation();
        }

        loadWarps(plugin);
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
    public static Location spawnPoint() { return spawnPoint.clone(); }
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
