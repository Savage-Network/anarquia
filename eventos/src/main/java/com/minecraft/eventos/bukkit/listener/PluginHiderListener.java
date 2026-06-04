package com.minecraft.eventos.bukkit.listener;

import com.minecraft.eventos.Core;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Set;

/** Oculta outros plugins nos comandos vanilla de listagem. */
public final class PluginHiderListener implements Listener {

    private static final Set<String> PLUGIN_CMDS = Set.of("plugins", "pl");
    private static final Set<String> VERSION_CMDS = Set.of("version", "ver");
    private static final Set<String> ABOUT_CMDS = Set.of("about");

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayer(PlayerCommandPreprocessEvent event) {
        intercept(event.getPlayer(), event.getMessage(), () -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsole(ServerCommandEvent event) {
        intercept(event.getSender(), "/" + event.getCommand(), () -> event.setCancelled(true));
    }

    private void intercept(CommandSender sender, String line, Runnable cancel) {
        Parsed cmd = parse(line);
        if (cmd == null) return;
        cancel.run();
        JavaPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) return;
        switch (cmd.type) {
            case PLUGINS -> sender.sendMessage(pluginsLine(plugin));
            case VERSION -> {
                for (String msg : versionLines(plugin, cmd.arg)) sender.sendMessage(msg);
            }
            case ABOUT -> {
                for (String msg : aboutLines(plugin, cmd.arg)) sender.sendMessage(msg);
            }
        }
    }

    private static String pluginsLine(JavaPlugin plugin) {
        String name = plugin.getDescription().getName();
        String color = plugin.isEnabled() ? "§a" : "§c";
        return "Plugins (1): " + color + name;
    }

    private static String[] versionLines(JavaPlugin plugin, String arg) {
        if (arg != null && !matchesSelf(plugin, arg)) {
            return new String[]{"§cPlugin desconhecido."};
        }
        PluginDescriptionFile desc = plugin.getDescription();
        return new String[]{
                "§fThis server is running §a" + Bukkit.getName() + " §f" + Bukkit.getBukkitVersion(),
                "§a" + desc.getName() + " §7v" + desc.getVersion()
                        + (desc.getDescription() != null && !desc.getDescription().isBlank()
                        ? " — " + desc.getDescription() : "")
        };
    }

    private static String[] aboutLines(JavaPlugin plugin, String arg) {
        if (arg != null && !matchesSelf(plugin, arg)) {
            return new String[]{"§cPlugin desconhecido."};
        }
        PluginDescriptionFile desc = plugin.getDescription();
        String authors = desc.getAuthors() == null || desc.getAuthors().isEmpty()
                ? "SavageLabs" : String.join(", ", desc.getAuthors());
        return new String[]{
                "§6§l" + desc.getName() + " §7v" + desc.getVersion(),
                "§7" + (desc.getDescription() == null ? "" : desc.getDescription()),
                "§7Autores: §f" + authors
        };
    }

    private static boolean matchesSelf(JavaPlugin plugin, String arg) {
        String a = arg.toLowerCase(Locale.ROOT);
        PluginDescriptionFile d = plugin.getDescription();
        return a.equals(d.getName().toLowerCase(Locale.ROOT))
                || a.equals(plugin.getName().toLowerCase(Locale.ROOT));
    }

    private static Parsed parse(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String line = raw.trim();
        if (line.startsWith("/")) line = line.substring(1);
        String[] parts = line.split("\\s+");
        if (parts.length == 0) return null;
        String root = parts[0].toLowerCase(Locale.ROOT);
        int colon = root.indexOf(':');
        if (colon >= 0) root = root.substring(colon + 1);
        String arg = parts.length > 1 ? parts[1] : null;
        if (PLUGIN_CMDS.contains(root)) return new Parsed(CmdType.PLUGINS, arg);
        if (VERSION_CMDS.contains(root)) return new Parsed(CmdType.VERSION, arg);
        if (ABOUT_CMDS.contains(root)) return new Parsed(CmdType.ABOUT, arg);
        return null;
    }

    private enum CmdType { PLUGINS, VERSION, ABOUT }

    private record Parsed(CmdType type, String arg) {}
}
