package com.minecraft.core.api.clan.color;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum ClanColor {

    BETA("Beta", "§1", "1", null, "beta"),
    CHAMPION("Champion", "§6", "6", null, "champion"),
    MOD("Mod", "§5", "5", null, "mod", "moderador"),
    EMBER("Ember", "§c", "c", null, "ember"),
    ADMIN("Admin", "§4", "4", null, "admin"),
    VERMELHO("Vermelho", "§d", "d", null, "vermelho"),
    CREATOR_PLUS("Creator+", "§3", "3", null, "creatorplus", "creator+"),
    BRASIL("Brasil", "§e", "brasil", "§eB§eR§eA§aS§aI§aL", "brasil"),
    YINGYANG("Yin Yang", "§f", "yingyang", "YINGYANG", "yingyang", "yin-yang", "yin");

    private final String name;
    private final String code;
    private final String id;
    private final String mixedColorTag;
    private final String[] aliases;

    ClanColor(String name, String code, String id, String mixedColorTag, String... aliases) {
        this.name = name;
        this.code = code;
        this.id = id;
        this.mixedColorTag = mixedColorTag;
        this.aliases = aliases;
    }

    public boolean hasMixedColorTag() {
        return mixedColorTag != null && !mixedColorTag.isEmpty();
    }

    public static String getDefaultColor() {
        return "§7";
    }

    public static String parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String normalized = input.trim().toLowerCase();

        if (normalized.startsWith("§") && normalized.length() == 2) {
            char code = normalized.charAt(1);
            for (ClanColor color : values()) {
                if (color.getCode().equals("§" + code)) {
                    return color.getCode();
                }
            }
        }

        if (normalized.startsWith("&") && normalized.length() == 2) {
            char code = normalized.charAt(1);
            for (ClanColor color : values()) {
                if (color.getCode().equals("§" + code)) {
                    return color.getCode();
                }
            }
        }

        for (ClanColor color : values()) {
            if (color.getId().equalsIgnoreCase(normalized)) {
                return color.getCode();
            }
        }

        for (ClanColor color : values()) {
            for (String alias : color.getAliases()) {
                if (alias.equalsIgnoreCase(normalized)) {
                    return color.getCode();
                }
            }
        }

        return null;
    }

    public static String getName(String colorCode) {
        if (colorCode == null) {
            return "Desconhecida";
        }

        for (ClanColor color : values()) {
            if (color.getCode().equals(colorCode)) {
                return color.getName();
            }
        }

        return "Desconhecida";
    }

    public static boolean isValid(String colorCode) {
        if (colorCode == null) {
            return false;
        }

        for (ClanColor color : values()) {
            if (color.getCode().equals(colorCode)) {
                return true;
            }
        }

        return false;
    }

//    public static List<String> getFormattedColors() {
//        return Arrays.stream(values())
//                .map(color -> color.getCode() + color.getName())
//                .collect(Collectors.toList());
//    }


    public static String getFormattedList() {
        StringBuilder builder = new StringBuilder("(");
        ClanColor[] colors = values();
        
        for (int i = 0; i < colors.length; i++) {
            ClanColor color = colors[i];
            builder.append(color.getCode()).append(color.getName());
            
            if (i < colors.length - 1) {
                builder.append("§c, ");
            }
        }
        
        builder.append("§c)");
        return builder.toString();
    }

    public static ClanColor getByCode(String colorCode) {
        if (colorCode == null) return null;
        for (ClanColor color : values()) {
            if (color.getCode().equals(colorCode)) return color;
        }
        return null;
    }

    public static ClanColor getById(String id) {
        if (id == null) return null;
        for (ClanColor color : values()) {
            if (color.getId().equalsIgnoreCase(id)) return color;
            for (String alias : color.getAliases()) {
                if (alias.equalsIgnoreCase(id)) return color;
            }
        }
        return null;
    }

//    public static ClanColor getByMixedColorTag(String mixedTag) {
//        if (mixedTag == null || mixedTag.trim().isEmpty()) {
//            return null;
//        }
//
//        String normalized = mixedTag.replace('&', '§');
//
//        for (ClanColor color : values()) {
//            if (color.hasMixedColorTag() && color.getMixedColorTag().equals(normalized)) {
//                return color;
//            }
//        }
//
//        return null;
//    }
//
//    public static List<ClanColor> getColorsWithMixedTags() {
//        return Arrays.stream(values())
//                .filter(ClanColor::hasMixedColorTag)
//                .collect(Collectors.toList());
//    }
}

