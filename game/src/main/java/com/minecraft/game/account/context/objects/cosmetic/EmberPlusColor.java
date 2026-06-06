package com.minecraft.game.account.context.objects.cosmetic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

/**
 * Cores do {@code +} do rank EMBER+ desbloqueadas por meses de assinatura.
 */
@Getter
@RequiredArgsConstructor
public enum EmberPlusColor {

    WHITE("white", "Branco", 1, Material.WHITE_DYE, "§f", false),
    AQUA("aqua", "Aqua", 1, Material.LIGHT_BLUE_DYE, "§b", false),
    RED("red", "Vermelho", 1, Material.RED_DYE, "§c", false),
    PINK("pink", "Rosa", 1, Material.PINK_DYE, "§d", false),
    YELLOW("yellow", "Amarelo", 1, Material.YELLOW_DYE, "§e", false),
    LIME("lime", "Lima", 1, Material.LIME_DYE, "§a", false),

    ORANGE("orange", "Laranja", 3, Material.ORANGE_DYE, "§6", false),
    GREEN("green", "Verde", 3, Material.GREEN_DYE, "§2", false),
    CYAN("cyan", "Ciano", 3, Material.CYAN_DYE, "§3", false),
    BLUE("blue", "Azul", 3, Material.BLUE_DYE, "§9", false),
    DARK_GRAY("dark_gray", "Cinza Escuro", 3, Material.GRAY_DYE, "§8", false),
    GRAY("gray", "Cinza", 3, Material.LIGHT_GRAY_DYE, "§7", false),

    BLACK("black", "Preto", 6, Material.BLACK_DYE, "§0", false),
    DARK_BLUE("dark_blue", "Azul Escuro", 6, Material.LAPIS_LAZULI, "§1", false),
    DARK_RED("dark_red", "Vermelho Escuro", 6, Material.NETHER_WART, "§4", false),
    PURPLE("purple", "Roxo", 6, Material.PURPLE_DYE, "§5", false),

    RAINBOW("rainbow", "Arco-íris", 12, Material.NETHER_STAR, null, true),
    YIN_YANG("yin_yang", "Yin Yang", 12, Material.QUARTZ, null, true);

    private final String id;
    private final String label;
    private final int minMonths;
    private final Material icon;
    private final String legacyColor;
    private final boolean animated;

    public String getDisplayName() {
        if (this == RAINBOW) return "§d§lArco-íris";
        if (this == YIN_YANG) return "§7§lYin Yang";
        return legacyColor + "§l" + label;
    }

    public boolean isUnlocked(int months) {
        return months >= minMonths;
    }

    public static EmberPlusColor fromId(String id) {
        if (id == null || id.isEmpty()) return PURPLE;
        for (EmberPlusColor color : values()) {
            if (color.id.equalsIgnoreCase(id)) return color;
        }
        return fromLegacy(id);
    }

    public static EmberPlusColor fromLegacy(String legacy) {
        if (legacy == null || legacy.isEmpty()) return PURPLE;
        for (EmberPlusColor color : values()) {
            if (!color.animated && legacy.equals(color.legacyColor)) return color;
        }
        return PURPLE;
    }
}
