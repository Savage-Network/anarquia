package com.minecraft.lobbyhost.display;

import com.minecraft.lobbyhost.Core;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Holograma "discord.gg/savagemc" usando {@link TextDisplay} 1.21.
 * Substitui o NMS {@code DiscordHologramManager} (ArmorStand) do Savage.
 */
public final class DiscordHologramManager {

    private static final NamespacedKey OWNER_KEY = NamespacedKey.minecraft("lobbyhost_discord_holo");

    private static final List<Entity> spawned = new ArrayList<>();

    public static void initialize() {
        cleanup();

        if (!Core.getJavaPlugin().getConfig().getBoolean("discord.hologram.enabled", false)) return;

        String worldName = Core.getJavaPlugin().getConfig().getString("discord.hologram.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Core.getLogger().warning("[DiscordHologram] Mundo '" + worldName + "' nao encontrado.");
            return;
        }

        double x = Core.getJavaPlugin().getConfig().getDouble("discord.hologram.x", 0.5);
        double y = Core.getJavaPlugin().getConfig().getDouble("discord.hologram.y", 80);
        double z = Core.getJavaPlugin().getConfig().getDouble("discord.hologram.z", 0.5);
        String url = Core.getJavaPlugin().getConfig().getString("discord.url", "discord.gg/savagemc");

        Location top = new Location(world, x, y + 0.3, z);
        Location bottom = new Location(world, x, y, z);

        spawned.add(spawnLine(top, Component.text("DISCORD", NamedTextColor.LIGHT_PURPLE)));
        spawned.add(spawnLine(bottom, Component.text(url, NamedTextColor.AQUA)));

        Core.getLogger().info("[DiscordHologram] " + spawned.size() + " linhas em " + worldName
                + " (" + x + "," + y + "," + z + ")");
    }

    private static TextDisplay spawnLine(Location location, Component text) {
        TextDisplay display = location.getWorld().spawn(location, TextDisplay.class, td -> {
            td.text(text);
            td.setBillboard(Display.Billboard.CENTER);
            td.setSeeThrough(true);
            td.setShadowed(true);
            td.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
            td.setPersistent(false);
            td.getPersistentDataContainer().set(OWNER_KEY, PersistentDataType.BOOLEAN, true);
        });
        return display;
    }

    public static void cleanup() {
        for (Entity e : new ArrayList<>(spawned)) {
            if (e != null && !e.isDead()) e.remove();
        }
        spawned.clear();

        // Limpar tambem entidades restantes em todos os mundos
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
