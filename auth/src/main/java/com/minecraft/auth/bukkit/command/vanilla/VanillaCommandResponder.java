package com.minecraft.auth.bukkit.command.vanilla;

import com.minecraft.auth.Core;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Mensagens bonitas ao interceptar comandos vanilla (auth). */
public final class VanillaCommandResponder {

    public static final String BYPASS_PERMISSION = "auth.commands.vanilla.bypass";

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String BRAND_A = "#FF8C42";
    private static final String BRAND_B = "#FFD166";
    private static final String MUTED = "#94A3B8";
    private static final String DIM = "#64748B";
    private static final String SUCCESS = "#3DDC97";

    private VanillaCommandResponder() {}

    public static void respond(Player player, String root, VanillaCommandCatalog.Category category) {
        if (player == null) return;
        switch (category) {
            case PLUGINS -> sendLines(player, List.of(
                    header(),
                    mini("Plugins (1): ") + "<" + SUCCESS + ">" + plainPluginName()
            ));
            case VERSION -> sendLines(player, versionLines());
            case HELP -> sendHelp(player);
            default -> sendLines(player, List.of(
                    header(),
                    mini(bodyFor(category, root)),
                    hintsLine(hintsFor(category))
            ));
        }
    }

    public static void unknown(Player player, String commandLine) {
        if (player == null) return;
        String root = "?";
        if (commandLine != null && !commandLine.isBlank()) {
            String line = commandLine.trim();
            if (line.startsWith("/")) line = line.substring(1);
            root = line.split("\\s+")[0];
        }
        sendLines(player, List.of(
                header(),
                mini("<red>Comando <yellow>/" + esc(root) + " <red>nao existe neste servidor."),
                hintsLine("login · register")
        ));
    }

    private static void sendHelp(Player player) {
        List<String> lines = new ArrayList<>();
        lines.add(header());
        lines.add(mini("Comandos do auth:"));
        lines.add(hint("login"));
        lines.add(hint("register"));
        lines.add(hint("changepassword"));
        lines.add(mini("<dark_gray>Autentique-se para entrar na rede."));
        sendLines(player, lines);
    }

    private static String bodyFor(VanillaCommandCatalog.Category category, String root) {
        String cmd = "/" + VanillaCommandCatalog.normalizeRoot(root);
        return switch (category) {
            case TELEPORT -> "<gray>Teleporte vanilla desativado.";
            case ADMIN -> "<gray>O comando <yellow>" + esc(cmd) + " <gray>e exclusivo da equipe.";
            case WORLD -> "<gray>Comandos de mundo nao estao liberados.";
            case SOCIAL -> "<gray>Use o chat normal do servidor.";
            case INFO -> "<gray>Informacao indisponivel via comando vanilla.";
            default -> "<gray>O comando <yellow>" + esc(cmd) + " <gray>nao esta disponivel aqui.";
        };
    }

    private static String hintsFor(VanillaCommandCatalog.Category category) {
        return switch (category) {
            case TELEPORT, ADMIN, WORLD, SOCIAL, INFO, DEFAULT -> "login · register";
            default -> "login · register";
        };
    }

    private static void sendLines(Player player, List<String> miniLines) {
        for (String mini : miniLines) {
            if (mini == null || mini.isBlank()) continue;
            player.sendMessage(parse(mini));
        }
    }

    private static Component parse(String mini) {
        return MM.deserialize(mini).decoration(TextDecoration.ITALIC, false);
    }

    private static String header() {
        return "<!italic><gradient:" + BRAND_A + ":" + BRAND_B + "><bold>SavageMC</bold></gradient>";
    }

    private static String mini(String text) {
        return "<!italic><" + MUTED + ">" + esc(text);
    }

    private static String hint(String command) {
        return "<!italic><" + DIM + ">/</" + DIM + "><" + MUTED + ">" + esc(command);
    }

    private static String hintsLine(String hints) {
        String[] parts = hints.split(" · ");
        StringBuilder sb = new StringBuilder(mini("Experimente: "));
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("<").append(DIM).append("> · ");
            sb.append(hint(parts[i].trim()));
        }
        return sb.toString();
    }

    private static String plainPluginName() {
        JavaPlugin plugin = Core.getJavaPlugin();
        return plugin != null ? plugin.getDescription().getName() : "auth";
    }

    private static List<String> versionLines() {
        JavaPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) {
            return List.of(header(), mini("Servidor SavageMC"));
        }
        PluginDescriptionFile desc = plugin.getDescription();
        return List.of(
                header(),
                "<!italic><#F1F5F9>Motor: <#F8FAFC>Paper " + Bukkit.getMinecraftVersion(),
                "<!italic><#F1F5F9>Rede: <#F8FAFC>" + desc.getName()
                        + " <" + MUTED + ">v" + desc.getVersion() + "</" + MUTED + ">",
                mini("savagemc.net")
        );
    }

    private static String esc(String text) {
        return text == null ? "" : text.replace("<", "\\<");
    }
}
