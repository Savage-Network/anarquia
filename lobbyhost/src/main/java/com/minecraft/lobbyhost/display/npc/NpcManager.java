package com.minecraft.lobbyhost.display.npc;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.architect.Architect;
import com.minecraft.lobbyhost.architect.SignedLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia spawn/remocao de {@link LobbyNpc}s a partir de {@code npcs.yml}
 * e chaves de localizacao do {@code architect.json}.
 */
public final class NpcManager {

    private static final Map<String, LobbyNpc> spawned = new ConcurrentHashMap<>();
    private static final Map<String, NpcDefinition> definitions = new HashMap<>();
    private static long clickCooldownMs = 500L;

    private NpcManager() {}

    public static void load(JavaPlugin plugin) {
        unloadAll();
        definitions.clear();

        File file = plugin.getDataFolder().toPath().resolve("npcs.yml").toFile();
        if (!file.exists()) {
            plugin.saveResource("npcs.yml", false);
        }
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        clickCooldownMs = yaml.getLong("click-cooldown-ms", 500L);

        if (!yaml.getBoolean("enabled", true)) {
            Core.getLogger().info("[NpcManager] NPCs desabilitados em npcs.yml.");
            return;
        }

        ConfigurationSection defs = yaml.getConfigurationSection("definitions");
        if (defs == null) return;

        for (String id : defs.getKeys(false)) {
            NpcDefinition def = NpcDefinition.fromConfig(id, defs.getConfigurationSection(id));
            if (def != null) definitions.put(id.toLowerCase(), def);
        }
        Core.getLogger().info("[NpcManager] " + definitions.size() + " definicoes carregadas.");
    }

    /** Spawna NPCs cujas {@code locationKey} existem no architect atual. */
    public static void spawnForArchitect(Architect architect) {
        unloadAll();
        if (definitions.isEmpty()) return;

        for (NpcDefinition def : definitions.values()) {
            SignedLocation signed = architect.location(def.getLocationKey());
            if (signed == null) {
                Core.getLogger().warning("[NpcManager] Location '" + def.getLocationKey()
                        + "' ausente no architect.json para NPC " + def.getId());
                continue;
            }
            Location loc = signed.toBukkit();
            if (loc.getWorld() == null) continue;
            try {
                LobbyNpc npc = new LobbyNpc(def, loc);
                spawned.put(def.getId().toLowerCase(), npc);
            } catch (Throwable t) {
                Core.getLogger().warning("[NpcManager] Falha ao spawnar " + def.getId() + ": " + t.getMessage());
            }
        }
        Core.getLogger().info("[NpcManager] " + spawned.size() + " NPCs ativos no mundo.");
    }

    public static void unloadAll() {
        for (LobbyNpc npc : spawned.values()) npc.remove();
        spawned.clear();
        cleanupOrphans();
    }

    /** Remove entidades NPC restantes (reload seguro). */
    public static void cleanupOrphans() {
        for (org.bukkit.World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {
                if (e.getPersistentDataContainer().has(LobbyNpc.NPC_ID_KEY, PersistentDataType.STRING)) {
                    e.remove();
                }
            }
        }
    }

    public static LobbyNpc get(String id) {
        return spawned.get(id.toLowerCase());
    }

    public static NpcDefinition getDefinition(String id) {
        return definitions.get(id.toLowerCase());
    }

    public static Collection<LobbyNpc> list() {
        return Collections.unmodifiableCollection(spawned.values());
    }

    public static Collection<NpcDefinition> listDefinitions() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public static long getClickCooldownMs() {
        return clickCooldownMs;
    }

    /** Executa acao configurada do NPC. */
    public static void handleClick(Player player, NpcDefinition def) {
        if (def == null) return;

        switch (def.getActionType()) {
            case MESSAGE -> {
                if (def.getActionValue() != null && !def.getActionValue().isBlank()) {
                    player.sendMessage(LobbyNpc.legacy(def.getActionValue()));
                }
            }
            case COMMAND -> {
                String cmd = def.getActionValue();
                if (cmd != null && !cmd.isBlank()) {
                    Bukkit.dispatchCommand(player, cmd.startsWith("/") ? cmd.substring(1) : cmd);
                }
            }
            case BUNGEE -> {
                String server = def.getActionValue();
                if (server == null || server.isBlank()) {
                    player.sendMessage(Component.text("§cServidor de destino nao configurado."));
                    return;
                }
                LobbyHostPlugin plugin = Core.getJavaPlugin();
                if (plugin == null) return;
                player.sendPluginMessage(plugin, "BungeeCord",
                        ("Connect\0" + server).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                player.sendMessage(Component.text("§aConectando em §f" + server + "§a..."));
            }
            case NONE -> {}
        }
    }

    /** Spawna NPC de teste na posicao do jogador (admin). */
    public static boolean spawnAtPlayer(Player player, String id) {
        NpcDefinition def = definitions.get(id.toLowerCase());
        if (def == null) return false;
        remove(id);
        LobbyNpc npc = new LobbyNpc(def, player.getLocation());
        spawned.put(id.toLowerCase(), npc);
        return true;
    }

    public static void remove(String id) {
        LobbyNpc npc = spawned.remove(id.toLowerCase());
        if (npc != null) npc.remove();
    }
}
