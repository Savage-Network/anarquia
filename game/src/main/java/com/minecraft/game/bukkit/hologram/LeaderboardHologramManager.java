package com.minecraft.game.bukkit.hologram;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.GameSettings;
import com.minecraft.game.backend.model.NetworkStatsEntry;
import com.minecraft.game.leaderboard.LeaderboardCache;
import com.minecraft.game.leaderboard.LeaderboardType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Hologramas TextDisplay dos rankings ({@link LeaderboardCache}). */
public final class LeaderboardHologramManager {

    private static final String DATA_FILE = "leaderboard-holograms.yml";
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final Map<LeaderboardType, TextDisplay> DISPLAYS = new EnumMap<>(LeaderboardType.class);

    private LeaderboardHologramManager() {}

    private static NamespacedKey holoKey() {
        return new NamespacedKey(Core.getJavaPlugin(), "leaderboard_holo");
    }

    public static void initialize() {
        cleanupAll();
        for (LeaderboardType type : LeaderboardType.values()) {
            if (isEnabled(type)) {
                spawn(type);
            }
        }
    }

    public static void refreshDisplays() {
        for (LeaderboardType type : LeaderboardType.values()) {
            TextDisplay display = DISPLAYS.get(type);
            if (display != null && display.isValid() && !display.isDead()) {
                display.text(buildText(type));
                continue;
            }
            if (isEnabled(type)) {
                spawn(type);
            }
        }
    }

    public static int activeCount() {
        int count = 0;
        for (LeaderboardType type : LeaderboardType.values()) {
            if (isEnabled(type)) count++;
        }
        return count;
    }

    public static boolean spawn(LeaderboardType type) {
        remove(type);
        Location location = resolveLocation(type);
        if (location == null || location.getWorld() == null) {
            Core.getLogger().warning("[LeaderboardHolo] Nao foi possivel spawnar '"
                    + type.holoId() + "' — mundo indisponivel.");
            return false;
        }

        TextDisplay display = location.getWorld().spawn(location, TextDisplay.class, entity -> {
            entity.text(buildText(type));
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setSeeThrough(true);
            entity.setShadowed(true);
            entity.setDefaultBackground(false);
            entity.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
            entity.setPersistent(false);
            entity.getPersistentDataContainer().set(holoKey(), PersistentDataType.STRING, type.name());
        });
        DISPLAYS.put(type, display);
        return true;
    }

    public static void remove(LeaderboardType type) {
        TextDisplay display = DISPLAYS.remove(type);
        if (display != null && display.isValid() && !display.isDead()) {
            display.remove();
        }
        purgeTagged(type);
    }

    public static void cleanupAll() {
        for (LeaderboardType type : LeaderboardType.values()) {
            remove(type);
        }
        DISPLAYS.clear();
        purgeAllTagged();
    }

    public static void shutdown() {
        cleanupAll();
    }

    public static void savePosition(LeaderboardType type, Location location) {
        YamlConfiguration cfg = loadData();
        String path = type.holoId();
        cfg.set(path + ".enabled", true);
        cfg.set(path + ".world", location.getWorld().getName());
        cfg.set(path + ".x", location.getX());
        cfg.set(path + ".y", location.getY());
        cfg.set(path + ".z", location.getZ());
        cfg.set(path + ".yaw", location.getYaw());
        cfg.set(path + ".pitch", location.getPitch());
        saveData(cfg);
    }

    public static void setEnabled(LeaderboardType type, boolean enabled) {
        YamlConfiguration cfg = loadData();
        cfg.set(type.holoId() + ".enabled", enabled);
        saveData(cfg);
    }

    public static boolean isEnabled(LeaderboardType type) {
        YamlConfiguration cfg = loadData();
        return cfg.getBoolean(type.holoId() + ".enabled", false);
    }

    public static Location getSavedLocation(LeaderboardType type) {
        YamlConfiguration cfg = loadData();
        String path = type.holoId();
        if (!cfg.contains(path + ".world")) return null;

        World world = Bukkit.getWorld(cfg.getString(path + ".world", GameSettings.SPAWN_WORLD));
        if (world == null) return null;

        return new Location(
                world,
                cfg.getDouble(path + ".x"),
                cfg.getDouble(path + ".y"),
                cfg.getDouble(path + ".z"),
                (float) cfg.getDouble(path + ".yaw"),
                (float) cfg.getDouble(path + ".pitch"));
    }

    public static Component buildText(LeaderboardType type) {
        return LEGACY.deserialize(buildLegacyText(type));
    }

    static String buildLegacyText(LeaderboardType type) {
        StringBuilder sb = new StringBuilder();
        sb.append("§8§m━━━━━━━━━━━━━━━━━━\n");
        sb.append(type.title()).append("\n");
        sb.append("§7Top 10 · atualizado ao vivo\n");
        sb.append("§8§m━━━━━━━━━━━━━━━━━━\n");

        List<NetworkStatsEntry> top = LeaderboardCache.get(type);
        if (top.isEmpty()) {
            sb.append("\n§7Nenhum dado ainda.");
            return sb.toString();
        }

        for (int i = 0; i < top.size(); i++) {
            sb.append(formatLine(i + 1, top.get(i), type)).append('\n');
        }
        return sb.toString().trim();
    }

    private static String formatLine(int rank, NetworkStatsEntry entry, LeaderboardType type) {
        String prefix = switch (rank) {
            case 1 -> "§6#" + rank;
            case 2 -> "§7#" + rank;
            case 3 -> "§c#" + rank;
            default -> "§8#" + rank;
        };
        String name = entry.getDisplayName() == null ? "?" : entry.getDisplayName();
        String value = switch (type) {
            case KILLS -> "§a" + entry.getKills() + " kills";
            case DEATHS -> "§c" + entry.getDeaths() + " mortes";
            case GOLDS -> "§a" + entry.getGolds() + " " + GameSettings.CURRENCY_SYMBOL;
            case KDR -> String.format(Locale.US, "§e%.2f K/D", entry.kdr());
            case CLANS -> "§f" + entry.getClanPower() + " poder §7(" + entry.getClanMembers() + " membros)";
        };
        return prefix + " §f" + name + " §8- " + value;
    }

    private static Location resolveLocation(LeaderboardType type) {
        Location saved = getSavedLocation(type);
        if (saved != null) return saved;
        return null;
    }

    private static void purgeTagged(LeaderboardType type) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                String id = entity.getPersistentDataContainer().get(holoKey(), PersistentDataType.STRING);
                if (type.name().equals(id) && !entity.isDead()) {
                    entity.remove();
                }
            }
        }
    }

    private static void purgeAllTagged() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getPersistentDataContainer().has(holoKey(), PersistentDataType.STRING)
                        && !entity.isDead()) {
                    entity.remove();
                }
            }
        }
    }

    private static YamlConfiguration loadData() {
        File file = dataFile();
        if (!file.exists()) {
            GamePlugin plugin = Core.getJavaPlugin();
            if (plugin.getResource(DATA_FILE) != null) {
                plugin.saveResource(DATA_FILE, false);
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private static void saveData(YamlConfiguration cfg) {
        try {
            cfg.save(dataFile());
        } catch (IOException e) {
            Core.getLogger().warning("[LeaderboardHolo] Falha ao salvar " + DATA_FILE + ": " + e.getMessage());
        }
    }

    private static File dataFile() {
        return new File(Core.getJavaPlugin().getDataFolder(), DATA_FILE);
    }
}
