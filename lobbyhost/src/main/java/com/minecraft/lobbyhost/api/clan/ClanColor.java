package com.minecraft.lobbyhost.api.clan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Estilos de clantag. Persistência sempre via {@link #getId()} (nunca {@code §code}).
 */
@Getter
@RequiredArgsConstructor
public enum ClanColor {

    // --- Sólidas ---
    DEFAULT("Padrão", "§7", "padrao", Material.LIGHT_GRAY_CONCRETE,
            StyleCategory.SOLID, true, null, RenderMode.SOLID, null, null),
    AQUA("Água", "§b", "aqua", Material.LIGHT_BLUE_CONCRETE,
            StyleCategory.SOLID, true, null, RenderMode.SOLID, null, null),
    GREEN("Verde", "§a", "verde", Material.LIME_CONCRETE,
            StyleCategory.SOLID, true, null, RenderMode.SOLID, null, null),
    GOLD("Ouro", "§6", "ouro", Material.ORANGE_CONCRETE,
            StyleCategory.SOLID, true, null, RenderMode.SOLID, null, null),
    RED("Vermelho", "§c", "vermelho", Material.RED_CONCRETE,
            StyleCategory.SOLID, true, null, RenderMode.SOLID, null, null),
    PINK("Rosa", "§d", "rosa", Material.PINK_CONCRETE,
            StyleCategory.SOLID, true, null, RenderMode.SOLID, null, null),
    PURPLE("Roxo", "§5", "roxo", Material.PURPLE_CONCRETE,
            StyleCategory.SOLID, true, null, RenderMode.SOLID, null, null),
    WHITE("Branco", "§f", "branco", Material.QUARTZ_BLOCK,
            StyleCategory.SOLID, true, null, RenderMode.SOLID, null, null),
    CYAN("Ciano", "§3", "ciano", Material.CYAN_CONCRETE,
            StyleCategory.SOLID, true, null, RenderMode.SOLID, null, null),

    // --- Premium ---
    EMBER("Ember", "§c", "ember", Material.MAGENTA_CONCRETE,
            StyleCategory.PREMIUM, false, "lobbyhost.vip", RenderMode.SOLID, null, null),
    ADMIN("Admin", "§4", "admin", Material.BLACK_CONCRETE,
            StyleCategory.PREMIUM, false, "lobbyhost.admin", RenderMode.SOLID, null, null),

    // --- Gradientes hex ---
    BRASIL("Brasil", "§a", "brasil", Material.GREEN_CONCRETE,
            StyleCategory.GRADIENT, true, null, RenderMode.HEX, "#009C3B", "#FFDF00"),
    SAVAGE("Savage", "§2", "savage", Material.EMERALD_BLOCK,
            StyleCategory.GRADIENT, true, null, RenderMode.HEX, "#064E3B", "#10B981"),
    SUNSET("Pôr do Sol", "§6", "sunset", Material.ORANGE_TERRACOTTA,
            StyleCategory.GRADIENT, true, null, RenderMode.HEX, "#EA580C", "#FBBF24"),
    OCEAN("Oceano", "§b", "oceano", Material.BLUE_CONCRETE,
            StyleCategory.GRADIENT, true, null, RenderMode.HEX, "#1D4ED8", "#38BDF8"),
    FOREST("Floresta", "§2", "floresta", Material.MOSS_BLOCK,
            StyleCategory.GRADIENT, true, null, RenderMode.HEX, "#14532D", "#4ADE80"),
    FIRE("Fogo", "§c", "fogo", Material.MAGMA_BLOCK,
            StyleCategory.GRADIENT, true, null, RenderMode.HEX, "#DC2626", "#FACC15"),
    AURORA("Aurora", "§d", "aurora", Material.AMETHYST_BLOCK,
            StyleCategory.GRADIENT, true, null, RenderMode.HEX, "#7C3AED", "#F472B6"),
    ICE("Gelo", "§b", "gelo", Material.PACKED_ICE,
            StyleCategory.GRADIENT, true, null, RenderMode.HEX, "#E0F2FE", "#22D3EE"),
    NEON("Neon", "§d", "neon", Material.PINK_GLAZED_TERRACOTTA,
            StyleCategory.GRADIENT, true, null, RenderMode.HEX, "#FF006E", "#00F5FF"),
    ROYAL("Royal", "§5", "royal", Material.PURPLE_GLAZED_TERRACOTTA,
            StyleCategory.GRADIENT, true, null, RenderMode.HEX, "#4C1D95", "#F59E0B"),
    MIDNIGHT("Meia-noite", "§1", "meianoite", Material.DEEPSLATE,
            StyleCategory.GRADIENT, true, null, RenderMode.HEX, "#0F172A", "#6366F1"),
    CHERRY("Cereja", "§c", "cereja", Material.CHERRY_LEAVES,
            StyleCategory.GRADIENT, true, null, RenderMode.HEX, "#BE185D", "#FDA4AF"),

    // --- Especiais ---
    YINGYANG("Yin Yang", "§f", "yingyang", Material.QUARTZ_BLOCK,
            StyleCategory.SPECIAL, true, null, RenderMode.YIN_YANG, null, null),
    ZEBRA("Zebra", "§f", "zebra", Material.WHITE_WOOL,
            StyleCategory.SPECIAL, true, null, RenderMode.ALTERNATE, "§f", "§8");

    private final String name;
    private final String code;
    private final String id;
    private final Material glass;
    private final StyleCategory category;
    private final boolean free;
    private final String permission;
    private final RenderMode renderMode;
    private final String toneA;
    private final String toneB;

    public boolean isGradient() {
        return renderMode == RenderMode.HEX;
    }

    public boolean isPremium() {
        return category == StyleCategory.PREMIUM;
    }

    @Deprecated
    public boolean hasMixedTag() {
        return renderMode != RenderMode.SOLID;
    }

    public static ClanColor defaultColor() {
        return DEFAULT;
    }

    public static List<ClanColor> byCategory(StyleCategory category) {
        List<ClanColor> out = new ArrayList<>();
        for (ClanColor color : values()) {
            if (color.category == category) out.add(color);
        }
        return out;
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
        return byId(normalizeStored(stored));
    }

    public static ClanColor byId(String id) {
        if (id == null || id.isBlank()) return DEFAULT;
        for (ClanColor color : values()) {
            if (color.id.equalsIgnoreCase(id)) return color;
        }
        return DEFAULT;
    }

    public static String normalizeStored(String stored) {
        if (stored == null || stored.isBlank()) return DEFAULT.getId();
        ClanColor parsed = parse(stored);
        if (parsed != null) return parsed.getId();
        for (ClanColor color : values()) {
            if (color.code.equals(stored)) return color.getId();
        }
        return DEFAULT.getId();
    }

    public enum StyleCategory {
        SOLID,
        PREMIUM,
        GRADIENT,
        SPECIAL
    }

    public enum RenderMode {
        SOLID,
        HEX,
        YIN_YANG,
        ALTERNATE
    }
}
