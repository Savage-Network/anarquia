package com.minecraft.game.bukkit.hologram;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.GameSettings;
import com.minecraft.game.message.Messages;
import com.minecraft.game.message.PlayerLanguage;
import com.minecraft.game.util.ModernText;
import net.kyori.adventure.text.Component;
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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Gerencia hologramas {@link TextDisplay} do game (posicoes em {@code holograms.yml}). */
public final class HologramManager {

    private static final String DATA_FILE = "holograms.yml";
    private static NamespacedKey holoIdKey() {
        return new NamespacedKey(Core.getJavaPlugin(), "hologram_id");
    }

    private static final Map<HologramType, List<Entity>> spawned = new EnumMap<>(HologramType.class);

    private HologramManager() {}

    public static void initialize() {
        cleanupAll();
        for (HologramType type : HologramType.values()) {
            if (isEnabled(type)) {
                spawn(type);
            }
        }
    }

    public static boolean spawn(HologramType type) {
        remove(type);
        Location location = resolveLocation(type);
        if (location == null || location.getWorld() == null) {
            Core.getLogger().warning("[Hologram] Nao foi possivel spawnar '" + type.id()
                    + "' — mundo indisponivel.");
            return false;
        }

        Component text = ModernText.parse(Messages.raw(type.messageKey(), PlayerLanguage.PT));
        TextDisplay display = spawnDisplay(location, text, type);
        spawned.computeIfAbsent(type, ignored -> new ArrayList<>()).add(display);

        Core.getLogger().info("[Hologram] '" + type.id() + "' em "
                + location.getWorld().getName() + " ("
                + fmt(location.getX()) + ", " + fmt(location.getY()) + ", " + fmt(location.getZ()) + ")");
        return true;
    }

    public static void remove(HologramType type) {
        List<Entity> entities = spawned.remove(type);
        if (entities != null) {
            for (Entity entity : entities) {
                if (entity != null && !entity.isDead()) entity.remove();
            }
        }
        purgeTagged(type);
    }

    public static void cleanupAll() {
        for (HologramType type : HologramType.values()) {
            remove(type);
        }
        spawned.clear();
        purgeAllTagged();
    }

    public static void shutdown() {
        cleanupAll();
    }

    public static void savePosition(HologramType type, Location location) {
        YamlConfiguration cfg = loadData();
        String path = type.id();
        cfg.set(path + ".enabled", true);
        cfg.set(path + ".world", location.getWorld().getName());
        cfg.set(path + ".x", location.getX());
        cfg.set(path + ".y", location.getY());
        cfg.set(path + ".z", location.getZ());
        cfg.set(path + ".yaw", location.getYaw());
        cfg.set(path + ".pitch", location.getPitch());
        saveData(cfg);
    }

    public static void setEnabled(HologramType type, boolean enabled) {
        YamlConfiguration cfg = loadData();
        cfg.set(type.id() + ".enabled", enabled);
        saveData(cfg);
    }

    public static boolean isEnabled(HologramType type) {
        YamlConfiguration cfg = loadData();
        return cfg.getBoolean(type.id() + ".enabled", type == HologramType.EXPLICACAO);
    }

    public static Location getSavedLocation(HologramType type) {
        YamlConfiguration cfg = loadData();
        String path = type.id();
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

    private static Location resolveLocation(HologramType type) {
        Location saved = getSavedLocation(type);
        if (saved != null) return saved;

        World world = Bukkit.getWorld(GameSettings.SPAWN_WORLD);
        if (world == null) return null;

        return new Location(
                world,
                GameSettings.SPAWN_X,
                GameSettings.SPAWN_Y + 2.8,
                GameSettings.SPAWN_Z,
                GameSettings.SPAWN_YAW,
                0f);
    }

    private static TextDisplay spawnDisplay(Location location, Component text, HologramType type) {
        return location.getWorld().spawn(location, TextDisplay.class, display -> {
            display.text(text);
            display.setBillboard(Display.Billboard.CENTER);
            display.setSeeThrough(true);
            display.setShadowed(true);
            display.setDefaultBackground(false);
            display.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
            display.setPersistent(false);
            display.getPersistentDataContainer().set(holoIdKey(), PersistentDataType.STRING, type.id());
        });
    }

    private static void purgeTagged(HologramType type) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                String id = entity.getPersistentDataContainer().get(holoIdKey(), PersistentDataType.STRING);
                if (type.id().equals(id) && !entity.isDead()) {
                    entity.remove();
                }
            }
        }
    }

    private static void purgeAllTagged() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getPersistentDataContainer().has(holoIdKey(), PersistentDataType.STRING)
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
            Core.getLogger().warning("[Hologram] Falha ao salvar holograms.yml: " + e.getMessage());
        }
    }

    private static File dataFile() {
        return new File(Core.getJavaPlugin().getDataFolder(), DATA_FILE);
    }

    private static String fmt(double value) {
        return String.format("%.1f", value);
    }
}
