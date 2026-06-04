package com.minecraft.proxyhost.velocity.listener;

import com.minecraft.proxyhost.ProxyHostPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** Oculta outros plugins no proxy: {@code /plugins}, {@code /pl}, {@code /version}, {@code /ver}, {@code /about}. */
public final class PluginHiderListener {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final Set<String> PLUGIN_CMDS = Set.of("plugins", "pl");
    private static final Set<String> VERSION_CMDS = Set.of("version", "ver");
    private static final Set<String> ABOUT_CMDS = Set.of("about");

    private final ProxyHostPlugin plugin;
    private final ProxyServer proxy;

    public PluginHiderListener(ProxyHostPlugin plugin) {
        this.plugin = plugin;
        this.proxy = plugin.getProxy();
    }

    @Subscribe
    public void onCommand(CommandExecuteEvent event) {
        if (!event.getResult().isAllowed()) return;

        Parsed parsed = parse(event.getCommand());
        if (parsed == null) return;

        event.setResult(CommandExecuteEvent.CommandResult.denied());

        Optional<PluginContainer> self = proxy.getPluginManager().getPlugin("proxyhost");
        if (self.isEmpty()) return;

        PluginDescription desc = self.get().getDescription();
        switch (parsed.type) {
            case PLUGINS -> event.getCommandSource().sendMessage(legacy(pluginsLine(desc)));
            case VERSION -> {
                for (String line : versionLines(desc, parsed.arg)) {
                    event.getCommandSource().sendMessage(legacy(line));
                }
            }
            case ABOUT -> {
                for (String line : aboutLines(desc, parsed.arg)) {
                    event.getCommandSource().sendMessage(legacy(line));
                }
            }
        }
    }

    private static String pluginsLine(PluginDescription desc) {
        return "Plugins (1): §a" + desc.getName().orElse("proxyhost");
    }

    private String[] versionLines(PluginDescription desc, String arg) {
        if (arg != null && !matchesSelf(desc, arg)) {
            return new String[]{"§cPlugin desconhecido."};
        }
        String name = desc.getName().orElse("proxyhost");
        String version = desc.getVersion().orElse("?");
        String description = desc.getDescription().orElse("");
        return new String[]{
                "§fThis server is running §aVelocity §f" + proxy.getVersion().getVersion(),
                "§a" + name + " §7v" + version + (description.isBlank() ? "" : " — " + description)
        };
    }

    private static String[] aboutLines(PluginDescription desc, String arg) {
        if (arg != null && !matchesSelf(desc, arg)) {
            return new String[]{"§cPlugin desconhecido."};
        }
        String name = desc.getName().orElse("proxyhost");
        String version = desc.getVersion().orElse("?");
        String description = desc.getDescription().orElse("");
        String authors = desc.getAuthors().isEmpty()
                ? "savage-anarquia" : String.join(", ", desc.getAuthors());
        return new String[]{
                "§6§l" + name + " §7v" + version,
                "§7" + description,
                "§7Autores: §f" + authors
        };
    }

    private static boolean matchesSelf(PluginDescription desc, String arg) {
        String a = arg.toLowerCase(Locale.ROOT);
        String name = desc.getName().orElse("proxyhost").toLowerCase(Locale.ROOT);
        String id = desc.getId().toLowerCase(Locale.ROOT);
        return a.equals(name) || a.equals(id);
    }

    private static Parsed parse(String commandLine) {
        if (commandLine == null || commandLine.isBlank()) return null;
        String[] parts = commandLine.trim().split("\\s+");
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

    private static Component legacy(String text) {
        return LEGACY.deserialize(text.replace('&', '§'));
    }

    private enum CmdType { PLUGINS, VERSION, ABOUT }

    private record Parsed(CmdType type, String arg) {}
}
