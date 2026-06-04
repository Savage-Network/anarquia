package com.minecraft.game.api.clan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

/**
 * Cores disponíveis para a clantag. Cada entrada mapeia para um vidro no menu.
 */
@Getter
@RequiredArgsConstructor
public enum ClanColor {

    DEFAULT("Padrão", "§7", "padrao", Material.LIGHT_GRAY_STAINED_GLASS_PANE, true, null),
    AQUA("Água", "§b", "aqua", Material.LIGHT_BLUE_STAINED_GLASS_PANE, true, null),
    GREEN("Verde", "§a", "verde", Material.LIME_STAINED_GLASS_PANE, true, null),
    GOLD("Ouro", "§6", "ouro", Material.ORANGE_STAINED_GLASS_PANE, true, null),
    RED("Vermelho", "§c", "vermelho", Material.RED_STAINED_GLASS_PANE, true, null),
    PINK("Rosa", "§d", "rosa", Material.PINK_STAINED_GLASS_PANE, true, null),
    PURPLE("Roxo", "§5", "roxo", Material.PURPLE_STAINED_GLASS_PANE, true, null),
    EMBER("Ember", "§c", "ember", Material.MAGENTA_STAINED_GLASS_PANE, false, "game.vip"),
    ADMIN("Admin", "§4", "admin", Material.BLACK_STAINED_GLASS_PANE, false, "game.admin"),
    BRASIL("Brasil", "§e", "brasil", Material.YELLOW_STAINED_GLASS_PANE, true, "brasil"),
    YINGYANG("Yin Yang", "§f", "yingyang", Material.WHITE_STAINED_GLASS_PANE, true, "yingyang");

    private final String name;
    private final String code;
    private final String id;
    private final Material glass;
    private final boolean free;
    /** Permissão LuckPerms; null = livre para todos. */
    private final String permission;

    public boolean hasMixedTag() {
        return this == BRASIL || this == YINGYANG;
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
}
