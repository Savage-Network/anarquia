package com.minecraft.lobbyhost.api.clan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

/**
 * Cores disponíveis para a clantag. Cada entrada mapeia para um vidro no menu.
 */
@Getter
@RequiredArgsConstructor
public enum ClanColor {

    DEFAULT("Padrão", "§7", "padrao", Material.LIGHT_GRAY_STAINED_GLASS_PANE, true, null,
            GradientMode.NONE, null, null),
    AQUA("Água", "§b", "aqua", Material.LIGHT_BLUE_STAINED_GLASS_PANE, true, null,
            GradientMode.NONE, null, null),
    GREEN("Verde", "§a", "verde", Material.LIME_STAINED_GLASS_PANE, true, null,
            GradientMode.NONE, null, null),
    GOLD("Ouro", "§6", "ouro", Material.ORANGE_STAINED_GLASS_PANE, true, null,
            GradientMode.NONE, null, null),
    RED("Vermelho", "§c", "vermelho", Material.RED_STAINED_GLASS_PANE, true, null,
            GradientMode.NONE, null, null),
    PINK("Rosa", "§d", "rosa", Material.PINK_STAINED_GLASS_PANE, true, null,
            GradientMode.NONE, null, null),
    PURPLE("Roxo", "§5", "roxo", Material.PURPLE_STAINED_GLASS_PANE, true, null,
            GradientMode.NONE, null, null),
    EMBER("Ember", "§c", "ember", Material.MAGENTA_STAINED_GLASS_PANE, false, "lobbyhost.vip",
            GradientMode.NONE, null, null),
    ADMIN("Admin", "§4", "admin", Material.BLACK_STAINED_GLASS_PANE, false, "lobbyhost.admin",
            GradientMode.NONE, null, null),

    /** Verde → amarelo (cores da bandeira). */
    BRASIL("Brasil", "§a", "brasil", Material.YELLOW_STAINED_GLASS_PANE, true, null,
            GradientMode.HEX, "#009C3B", "#FFDF00"),
    SUNSET("Pôr do Sol", "§6", "sunset", Material.ORANGE_STAINED_GLASS_PANE, true, null,
            GradientMode.HEX, "#EA580C", "#FBBF24"),
    OCEAN("Oceano", "§b", "oceano", Material.BLUE_STAINED_GLASS_PANE, true, null,
            GradientMode.HEX, "#1D4ED8", "#38BDF8"),
    FOREST("Floresta", "§2", "floresta", Material.GREEN_STAINED_GLASS_PANE, true, null,
            GradientMode.HEX, "#14532D", "#4ADE80"),
    FIRE("Fogo", "§c", "fogo", Material.RED_STAINED_GLASS_PANE, true, null,
            GradientMode.HEX, "#DC2626", "#FACC15"),
    AURORA("Aurora", "§d", "aurora", Material.MAGENTA_STAINED_GLASS_PANE, true, null,
            GradientMode.HEX, "#7C3AED", "#F472B6"),
    ICE("Gelo", "§b", "gelo", Material.CYAN_STAINED_GLASS_PANE, true, null,
            GradientMode.HEX, "#E0F2FE", "#22D3EE"),
    SAVAGE("Savage", "§2", "savage", Material.EMERALD_BLOCK, true, null,
            GradientMode.HEX, "#064E3B", "#10B981"),
    YINGYANG("Yin Yang", "§f", "yingyang", Material.WHITE_STAINED_GLASS_PANE, true, null,
            GradientMode.YIN_YANG, null, null);

    private final String name;
    private final String code;
    private final String id;
    private final Material glass;
    private final boolean free;
    /** Permissão LuckPerms; null = livre para todos. */
    private final String permission;
    private final GradientMode gradientMode;
    private final String gradientFrom;
    private final String gradientTo;

    public boolean hasMixedTag() {
        return gradientMode != GradientMode.NONE;
    }

    public static ClanColor parse(String input) {
        if (input == null || input.isBlank()) return null;
        String n = input.trim().toLowerCase();
        if (n.length() == 2 && (n.startsWith("§") || n.startsWith("&"))) {
            char c = n.charAt(1);
            for (ClanColor color : values()) {
                if (color.code.equals("§" + c)) return color;
            }
        }
        for (ClanColor color : values()) {
            if (color.id.equalsIgnoreCase(n) || color.name.equalsIgnoreCase(n)) return color;
        }
        return null;
    }

    public static ClanColor byStored(String stored) {
        if (stored == null || stored.isBlank()) return DEFAULT;
        ClanColor byId = parse(stored);
        if (byId != null) return byId;
        for (ClanColor color : values()) {
            if (color.code.equals(stored)) return color;
        }
        return DEFAULT;
    }

    public enum GradientMode {
        NONE,
        HEX,
        YIN_YANG
    }
}
