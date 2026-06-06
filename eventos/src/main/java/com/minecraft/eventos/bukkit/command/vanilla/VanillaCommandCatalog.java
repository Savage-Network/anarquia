package com.minecraft.eventos.bukkit.command.vanilla;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Mapeia comandos vanilla do Minecraft para categorias de resposta. */
public final class VanillaCommandCatalog {

    public enum Category {
        PLUGINS,
        VERSION,
        HELP,
        TELEPORT,
        ADMIN,
        WORLD,
        SOCIAL,
        INFO,
        DEFAULT
    }

    private static final Map<String, Category> BY_ROOT = new HashMap<>();

    static {
        map(Category.PLUGINS, "plugins", "pl", "bukkit:plugins", "bukkit:pl");
        map(Category.VERSION, "version", "ver", "about", "bukkit:version", "bukkit:about");
        map(Category.HELP, "help", "?", "bukkit:help");
        map(Category.TELEPORT,
                "tp", "teleport", "tphere", "tppos", "spreadplayers", "spawnpoint",
                "setworldspawn", "worldspawn", "spectate", "ride");
        map(Category.ADMIN,
                "give", "item", "clear", "gamemode", "gm", "gamerule", "effect", "enchant",
                "experience", "xp", "kill", "damage", "attribute", "op", "deop",
                "ban", "ban-ip", "banlist", "pardon", "pardon-ip", "kick", "whitelist",
                "permission", "perm", "luck", "advancement", "recipe", "loot",
                "publish", "transfer", "jfr", "perf", "debug", "return", "execute",
                "function", "schedule", "random", "tick", "raid", "bossbar", "forceload",
                "datapack", "reload", "save-all", "save-off", "save-on", "stop", "restart");
        map(Category.WORLD,
                "setblock", "fill", "clone", "summon", "particle", "playsound", "stopsound",
                "title", "weather", "time", "day", "night", "difficulty", "defaultgamemode",
                "worldborder", "fillbiome", "place", "locate", "locatebiome", "seed",
                "setidletimeout", "data");
        map(Category.SOCIAL, "me", "msg", "tell", "w", "teammsg", "tm", "say", "broadcast");
        map(Category.INFO, "list", "trigger", "scoreboard", "team", "tag");
    }

    private VanillaCommandCatalog() {}

    private static void map(Category category, String... roots) {
        for (String root : roots) {
            BY_ROOT.put(normalizeRoot(root), category);
        }
    }

    public static String normalizeRoot(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String root = raw.trim().toLowerCase(Locale.ROOT);
        if (root.startsWith("/")) root = root.substring(1);
        int colon = root.indexOf(':');
        if (colon >= 0) root = root.substring(colon + 1);
        return root;
    }

    public static Category category(String root) {
        Category cat = BY_ROOT.get(normalizeRoot(root));
        return cat != null ? cat : Category.DEFAULT;
    }

    public static boolean isKnownVanilla(String root) {
        return BY_ROOT.containsKey(normalizeRoot(root));
    }
}
