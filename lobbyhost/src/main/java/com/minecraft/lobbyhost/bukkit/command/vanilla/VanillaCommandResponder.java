package com.minecraft.lobbyhost.bukkit.command.vanilla;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.message.LanguageService;
import com.minecraft.lobbyhost.message.Messages;
import com.minecraft.lobbyhost.message.PlayerLanguage;
import com.minecraft.lobbyhost.util.ModernText;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Mensagens bonitas ao interceptar comandos vanilla. */
public final class VanillaCommandResponder {

    public static final String BYPASS_PERMISSION = "lobbyhost.commands.vanilla.bypass";

    private VanillaCommandResponder() {}

    public static void respond(Player player, String root, VanillaCommandCatalog.Category category) {
        if (player == null) return;
        PlayerLanguage lang = LanguageService.of(player);

        switch (category) {
            case PLUGINS -> sendLines(player, List.of(
                    header(),
                    Messages.format("vanilla.plugins.line", lang,
                            Map.of("plugin", plainPluginName()))
            ));
            case VERSION -> sendLines(player, versionLines());
            case HELP -> sendHelp(player, lang);
            default -> sendLines(player, List.of(
                    header(),
                    Messages.format("vanilla." + category.name().toLowerCase(Locale.ROOT) + ".body", lang,
                            Map.of("command", "/" + VanillaCommandCatalog.normalizeRoot(root))),
                    hintsLine(hintsFor(category))
            ));
        }
    }

    public static void unknown(Player player, String commandLine) {
        if (player == null) return;
        PlayerLanguage lang = LanguageService.of(player);
        String root = "?";
        if (commandLine != null && !commandLine.isBlank()) {
            String line = commandLine.trim();
            if (line.startsWith("/")) line = line.substring(1);
            root = line.split("\\s+")[0];
        }

        sendLines(player, List.of(
                header(),
                Messages.format("vanilla.unknown.body", lang, Map.of("command", "/" + root)),
                hintsLine(defaultHints())
        ));
    }

    private static void sendHelp(Player player, PlayerLanguage lang) {
        List<String> lines = new ArrayList<>();
        lines.add(header());
        lines.add(Messages.format("vanilla.help.intro", lang));
        for (String hint : hintCommands()) {
            lines.add(ModernText.hint(hint));
        }
        lines.add(Messages.format("vanilla.help.footer", lang));
        sendLines(player, lines);
    }

    private static void sendLines(Player player, List<String> miniLines) {
        for (String mini : miniLines) {
            if (mini == null || mini.isBlank()) continue;
            player.sendMessage(ModernText.noItalic(ModernText.parse(mini)));
        }
    }

    private static String header() {
        return ModernText.brandGradient("SavageMC");
    }

    private static String hintsLine(String hints) {
        String[] parts = hints.split(" · ");
        StringBuilder sb = new StringBuilder(ModernText.mini("Experimente: "));
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append("<").append(ModernText.DIM).append("> · ");
            }
            sb.append(ModernText.hint(parts[i].trim()));
        }
        return sb.toString();
    }

    private static String hintsFor(VanillaCommandCatalog.Category category) {
        return switch (category) {
            case TELEPORT -> "home · warp · spawn · tpa · rtp";
            case ADMIN -> "report · livro";
            case WORLD -> "spawn · livro";
            case SOCIAL -> "cc · amigo";
            case INFO -> "perfil · stats · top";
            default -> defaultHints();
        };
    }

    private static String defaultHints() {
        return switch (serverProfile()) {
            case "lobbyhost" -> "play · amigo · prefs · perfil";
            case "auth" -> "login · register";
            case "eventos" -> "leave · team";
            default -> "livro · loja · kit · spawn · stats";
        };
    }

    private static List<String> hintCommands() {
        return switch (serverProfile()) {
            case "lobbyhost" -> List.of("play", "amigo", "prefs", "perfil", "leaderboard");
            case "auth" -> List.of("login", "register");
            case "eventos" -> List.of("leave", "team");
            default -> List.of("livro", "loja", "kit", "spawn", "home", "stats", "clan");
        };
    }

    private static String serverProfile() {
        JavaPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) return "game";
        return plugin.getName().toLowerCase(Locale.ROOT);
    }

    private static String plainPluginName() {
        JavaPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) return "SavageMC";
        return plugin.getDescription().getName();
    }

    private static List<String> versionLines() {
        JavaPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) {
            return List.of(header(), ModernText.mini("Servidor SavageMC"));
        }
        PluginDescriptionFile desc = plugin.getDescription();
        return List.of(
                header(),
                ModernText.label("Motor: ") + "<#F8FAFC>Paper " + Bukkit.getMinecraftVersion(),
                ModernText.label("Rede: ") + "<#F8FAFC>" + desc.getName()
                        + " <" + ModernText.MUTED + ">v" + desc.getVersion() + "</" + ModernText.MUTED + ">",
                ModernText.mini("savagemc.net")
        );
    }
}
