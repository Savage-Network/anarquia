package com.minecraft.lobbyhost.display;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.config.DiscordConfig;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.util.ModernText;
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
import java.util.List;

/**
 * Holograma do Discord ({@link TextDisplay}). Posicao em {@code discord-hologram.yml}
 * (definida com /setdiscord), fora do config.yml principal.
 */
public final class DiscordHologramManager {

    private static final NamespacedKey OWNER_KEY = NamespacedKey.minecraft("lobbyhost_discord_holo");
    private static final String DATA_FILE = "discord-hologram.yml";

    private static final List<Entity> spawned = new ArrayList<>();

    private DiscordHologramManager() {}

    public static void initialize() {
        cleanup();
        YamlConfiguration cfg = loadData();
        if (!DiscordConfig.isEnabled() || !DiscordConfig.feature("lobby-hologram")) return;

        String worldName = cfg.getString("world", LobbySettings.DEFAULT_SPAWN_WORLD);
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Core.getLogger().warning("[DiscordHologram] Mundo '" + worldName + "' nao encontrado.");
            return;
        }

        double x = cfg.getDouble("x", LobbySettings.DEFAULT_SPAWN_X);
        double y = cfg.getDouble("y", LobbySettings.DEFAULT_SPAWN_Y + 3);
        double z = cfg.getDouble("z", LobbySettings.DEFAULT_SPAWN_Z);
        String url = DiscordConfig.inviteDisplay();
        String holoTitle = DiscordConfig.message("hologram-title");
        if (holoTitle.isBlank()) holoTitle = "DISCORD";

        spawned.add(spawnLine(new Location(world, x, y + 0.3, z),
                ModernText.parse(
                    "<!italic><gradient:#C084FC:#818CF8><bold>" + holoTitle + "</bold></gradient>\n"
                  + "<#38BDF8>" + url + "\n"
                  + "<" + ModernText.BRAND_B + ">" + LobbySettings.SERVER_ADDRESS + "\n"
                  + "<#94A3B8>Loja <" + ModernText.BRAND_B + ">" + LobbySettings.SERVER_STORE_URL
                )));

        Core.getLogger().info("[DiscordHologram] " + spawned.size() + " linhas em " + worldName
                + " (" + x + "," + y + "," + z + ")");
    }

    public static void savePosition(Location loc) {
        YamlConfiguration cfg = loadData();
        cfg.set("enabled", true);
        cfg.set("world", loc.getWorld().getName());
        cfg.set("x", loc.getX());
        cfg.set("y", loc.getY());
        cfg.set("z", loc.getZ());
        saveData(cfg);
    }

    private static YamlConfiguration loadData() {
        File file = dataFile();
        if (!file.exists()) {
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private static void saveData(YamlConfiguration cfg) {
        try {
            cfg.save(dataFile());
        } catch (IOException e) {
            Core.getLogger().warning("Falha ao salvar discord-hologram.yml: " + e.getMessage());
        }
    }

    private static File dataFile() {
        LobbyHostPlugin plugin = Core.getJavaPlugin();
        return new File(plugin.getDataFolder(), DATA_FILE);
    }

    private static TextDisplay spawnLine(Location location, Component text) {
        return location.getWorld().spawn(location, TextDisplay.class, td -> {
            td.text(text);
            td.setBillboard(Display.Billboard.CENTER);
            td.setSeeThrough(true);
            td.setShadowed(true);
            td.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
            td.setPersistent(false);
            td.getPersistentDataContainer().set(OWNER_KEY, PersistentDataType.BOOLEAN, true);
        });
    }

    public static void cleanup() {
        for (Entity e : new ArrayList<>(spawned)) {
            if (e != null && !e.isDead()) e.remove();
        }
        spawned.clear();

        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e.getPersistentDataContainer().has(OWNER_KEY, PersistentDataType.BOOLEAN)) {
                    e.remove();
                }
            }
        }
    }

    public static void shutdown() {
        cleanup();
    }
}
