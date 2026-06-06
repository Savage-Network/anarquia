package com.minecraft.eventos.arena;

import com.minecraft.eventos.EventosPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Persistencia YAML de arenas ({@code arenas.yml}). */
public final class ArenaRepository {

    private final EventosPlugin plugin;
    private File file;
    private FileConfiguration yaml;
    private final Map<String, ArenaDefinition> cache = new LinkedHashMap<>();

    public ArenaRepository(EventosPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "arenas.yml");
        if (!file.exists()) {
            plugin.saveResource("arenas.yml", false);
        }
        yaml = YamlConfiguration.loadConfiguration(file);
        mergeDefaults();
        cache.clear();
        ConfigurationSection root = yaml.getConfigurationSection("arenas");
        if (root == null) return;
        for (String id : root.getKeys(false)) {
            ArenaDefinition arena = read(id, root.getConfigurationSection(id));
            if (arena != null) cache.put(id.toLowerCase(), arena);
        }
    }

    public void save(ArenaDefinition arena) {
        if (arena == null || arena.getId() == null) return;
        cache.put(arena.getId().toLowerCase(), arena);
        yaml.set("arenas." + arena.getId() + ".mode", arena.getModeId());
        yaml.set("arenas." + arena.getId() + ".world", arena.getWorldName());
        yaml.set("arenas." + arena.getId() + ".center.x", arena.getCenterX());
        yaml.set("arenas." + arena.getId() + ".center.y", arena.getCenterY());
        yaml.set("arenas." + arena.getId() + ".center.z", arena.getCenterZ());
        yaml.set("arenas." + arena.getId() + ".center.yaw", arena.getCenterYaw());
        yaml.set("arenas." + arena.getId() + ".center.pitch", arena.getCenterPitch());
        yaml.set("arenas." + arena.getId() + ".radius", arena.getRadius());
        yaml.set("arenas." + arena.getId() + ".die-y", arena.getDieY());
        yaml.set("arenas." + arena.getId() + ".width", arena.getWidth());
        yaml.set("arenas." + arena.getId() + ".depth", arena.getDepth());
        yaml.set("arenas." + arena.getId() + ".generated", arena.isGenerated());
        yaml.set("arenas." + arena.getId() + ".generated-at", arena.getGeneratedAt());
        List<Map<String, Object>> spawns = new ArrayList<>();
        for (ArenaDefinition.SpawnPoint sp : arena.getSpawns()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("world", sp.worldName());
            m.put("x", sp.x());
            m.put("y", sp.y());
            m.put("z", sp.z());
            m.put("yaw", sp.yaw());
            m.put("pitch", sp.pitch());
            spawns.add(m);
        }
        yaml.set("arenas." + arena.getId() + ".spawns", spawns);
        try {
            yaml.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("[Arenas] save: " + e.getMessage());
        }
    }

    public Optional<ArenaDefinition> find(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return Optional.ofNullable(cache.get(id.toLowerCase()));
    }

    public Collection<ArenaDefinition> all() {
        return Collections.unmodifiableCollection(cache.values());
    }

    public void delete(String id) {
        if (id == null) return;
        cache.remove(id.toLowerCase());
        yaml.set("arenas." + id, null);
        try {
            yaml.save(file);
        } catch (Exception ignored) {
        }
    }

    private ArenaDefinition read(String id, ConfigurationSection s) {
        if (s == null) return null;
        ArenaDefinition a = new ArenaDefinition();
        a.setId(id);
        a.setModeId(s.getString("mode", "sumo"));
        a.setWorldName(s.getString("world", "world"));
        a.setCenterX(s.getDouble("center.x", 0));
        a.setCenterY(s.getDouble("center.y", 64));
        a.setCenterZ(s.getDouble("center.z", 0));
        a.setCenterYaw((float) s.getDouble("center.yaw", 0));
        a.setCenterPitch((float) s.getDouble("center.pitch", 0));
        a.setRadius(s.getInt("radius", 14));
        a.setDieY(s.getInt("die-y", 50));
        a.setWidth(s.getInt("width", 15));
        a.setDepth(s.getInt("depth", 15));
        a.setGenerated(s.getBoolean("generated", false));
        a.setGeneratedAt(s.getLong("generated-at", 0));
        List<ArenaDefinition.SpawnPoint> spawns = new ArrayList<>();
        if (s.isList("spawns")) {
            for (Object o : s.getList("spawns", List.of())) {
                if (o instanceof Map<?, ?> map) {
                    spawns.add(new ArenaDefinition.SpawnPoint(
                            String.valueOf(map.get("world")),
                            toDouble(map.get("x")),
                            toDouble(map.get("y")),
                            toDouble(map.get("z")),
                            (float) toDouble(map.get("yaw")),
                            (float) toDouble(map.get("pitch"))));
                }
            }
        }
        a.setSpawns(spawns);
        return a;
    }

    private static double toDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception e) {
            return 0;
        }
    }

    private void mergeDefaults() {
        try (InputStream in = plugin.getResource("arenas.yml")) {
            if (in == null) return;
            yaml.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8)));
            yaml.options().copyDefaults(true);
            yaml.save(file);
        } catch (Exception ignored) {
        }
    }
}
