package com.minecraft.game.account.context.objects.rank;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Hierarquia de ranks (ordinal == nivel). Espelha
 * {@code com.minecraft.core.account.rank.RankType} do Savage.
 *
 * <p>{@link #isAtLeast(RankType)} compara por ordinal. Tags VIP/STAFF
 * referenciam o rank em {@code Tag.source}.
 */
@Getter
@RequiredArgsConstructor
public enum RankType {

    MEMBER     ("game.rank.member",      "§7"),

    /* ---------- VIP ---------- */
    VIP        ("game.rank.vip",         "§a"),
    NATIVE     ("game.rank.native",      "§b"),
    EMBER      ("game.rank.ember",       "§6"),
    EMBER_PLUS ("game.rank.ember_plus",  "§6"),
    SAVAGE     ("game.rank.savage",      "§2"),

    /* ---------- SPECIAL ---------- */
    BETA       ("game.rank.beta",        "§e"),
    PRECIOUS   ("game.rank.precious",    "§5"),

    /* ---------- STAFF ---------- */
    BUILDER    ("game.rank.builder",     "§3"),
    HELPER     ("game.rank.helper",      "§9"),
    MOD        ("game.rank.mod",         "§3"),
    MODPLUS    ("game.rank.modplus",     "§3"),
    ADMIN      ("game.rank.admin",       "§4"),
    DIRETOR    ("game.rank.diretor",     "§4"),
    VERMELHO   ("game.rank.vermelho",    "§c"),
    OWNER      ("game.rank.owner",       "§4");

    private final String permission;
    private final String color;

    public boolean isAtLeast(RankType other) {
        return ordinal() >= other.ordinal();
    }

    /** Staff comeca em BUILDER (ordinal). */
    public boolean isStaff() {
        return isAtLeast(BUILDER);
    }

    /**
     * Nos Bukkit aplicados via {@link com.minecraft.game.bukkit.manager.PermissionManager}.
     * Inclui permissoes herdadas da hierarquia (VIP, staff, admin).
     */
    public java.util.List<String> getPermissionNodes() {
        java.util.List<String> nodes = new java.util.ArrayList<>();
        nodes.add(permission);

        if (isAtLeast(VIP)) {
            nodes.add("game.vip");
        }
        if (isStaff()) {
            nodes.add("game.staff");
        }
        if (isAtLeast(ADMIN)) {
            nodes.add("game.admin");
            nodes.add("game.*");
        }
        if (this == OWNER || this == DIRETOR || this == VERMELHO) {
            nodes.add("*");
        }
        return nodes;
    }

    /** Verifica se este rank concede o no (suporta {@code game.*} e {@code *}). */
    public boolean grants(String key) {
        if (key == null || key.isEmpty()) return false;
        for (String node : getPermissionNodes()) {
            if (matchesPermission(node, key)) return true;
        }
        return false;
    }

    static boolean matchesPermission(String held, String required) {
        if (held == null || required == null) return false;
        if ("*".equals(held)) return true;
        if (held.equalsIgnoreCase(required)) return true;
        if (held.endsWith(".*")) {
            String prefix = held.substring(0, held.length() - 2);
            return required.length() >= prefix.length()
                    && required.regionMatches(true, 0, prefix, 0, prefix.length())
                    && (required.length() == prefix.length() || required.charAt(prefix.length()) == '.');
        }
        return false;
    }
}
