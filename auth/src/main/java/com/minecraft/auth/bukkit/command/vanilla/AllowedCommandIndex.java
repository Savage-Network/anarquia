package com.minecraft.auth.bukkit.command.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Comandos permitidos (plugin do servidor + parceiros comuns). */
public final class AllowedCommandIndex {

    private static final Set<String> GLOBAL_PARTNERS = Set.of(
            "lp", "luckperms", "papi", "placeholderapi", "plugman",
            "worldedit", "we", "fawe", "fastasyncworldedit", "viaversion", "vivecraft"
    );

    private static final Set<String> VANILLA_OWNERS = Set.of(
            "minecraft", "bukkit", "paper", "spigot", "craftbukkit"
    );

    private static final Set<String> ALLOWED = new HashSet<>();

    private AllowedCommandIndex() {}

    public static void rebuild(JavaPlugin plugin) {
        ALLOWED.clear();
        if (plugin != null) {
            indexPluginYaml(plugin);
        }
        for (Plugin loaded : Bukkit.getPluginManager().getPlugins()) {
            if (loaded == null || !loaded.isEnabled()) continue;
            if (isVanillaOwner(loaded.getName())) continue;
            indexPluginCommands(loaded);
        }
        ALLOWED.addAll(GLOBAL_PARTNERS);
    }

    public static boolean isAllowed(String root) {
        if (root == null || root.isBlank()) return false;
        return ALLOWED.contains(VanillaCommandCatalog.normalizeRoot(root));
    }

    public static boolean isVanillaOwner(String pluginName) {
        if (pluginName == null) return true;
        return VANILLA_OWNERS.contains(pluginName.toLowerCase(Locale.ROOT));
    }

    public static boolean isVanillaCommand(String root) {
        String normalized = VanillaCommandCatalog.normalizeRoot(root);
        Command command = Bukkit.getCommandMap().getCommand(normalized);
        if (command instanceof PluginCommand pc) {
            Plugin owner = pc.getPlugin();
            if (owner == null) return true;
            return isVanillaOwner(owner.getName());
        }
        return VanillaCommandCatalog.isKnownVanilla(normalized);
    }

    public static Set<String> allowedSnapshot() {
        return Collections.unmodifiableSet(ALLOWED);
    }

    private static void indexPluginYaml(JavaPlugin plugin) {
        Map<String, Map<String, Object>> commands = plugin.getDescription().getCommands();
        if (commands == null) return;
        for (Map.Entry<String, Map<String, Object>> entry : commands.entrySet()) {
            addRoot(entry.getKey());
            Object aliases = entry.getValue().get("aliases");
            if (aliases instanceof List<?> list) {
                for (Object alias : list) {
                    if (alias != null) addRoot(alias.toString());
                }
            } else if (aliases instanceof String single) {
                addRoot(single);
            }
        }
    }

    private static void indexPluginCommands(Plugin plugin) {
        for (Command command : Bukkit.getCommandMap().getKnownCommands().values()) {
            if (!(command instanceof PluginCommand pc)) continue;
            if (pc.getPlugin() == null || !pc.getPlugin().equals(plugin)) continue;
            addRoot(command.getName());
            if (command.getAliases() != null) {
                command.getAliases().forEach(AllowedCommandIndex::addRoot);
            }
        }
    }

    private static void addRoot(String root) {
        if (root == null || root.isBlank()) return;
        ALLOWED.add(VanillaCommandCatalog.normalizeRoot(root));
    }
}
