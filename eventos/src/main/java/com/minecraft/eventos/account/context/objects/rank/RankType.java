package com.minecraft.eventos.account.context.objects.rank;

import com.minecraft.eventos.account.context.objects.tag.LegacyHex;
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

    MEMBER     ("game.rank.member",      LegacyHex.MEMBER),

    /* ---------- VIP ---------- */
    VIP        ("game.rank.vip",         LegacyHex.VIP),
    NATIVE     ("game.rank.native",      LegacyHex.NATIVE),
    EMBER      ("game.rank.ember",       LegacyHex.EMBER),
    EMBER_PLUS ("game.rank.ember_plus",  LegacyHex.EMBER),
    SAVAGE     ("game.rank.savage",      LegacyHex.SAVAGE),

    /* ---------- SPECIAL ---------- */
    BETA       ("game.rank.beta",        LegacyHex.BETA),
    PRECIOUS   ("game.rank.precious",    LegacyHex.PRECIOUS),

    /* ---------- STAFF ---------- */
    BUILDER    ("game.rank.builder",     LegacyHex.BUILDER),
    HELPER     ("game.rank.helper",      LegacyHex.HELPER),
    MOD        ("game.rank.mod",         LegacyHex.MOD),
    MODPLUS    ("game.rank.modplus",     LegacyHex.MOD),
    ADMIN      ("game.rank.admin",       LegacyHex.ADMIN),
    DIRETOR    ("game.rank.diretor",     LegacyHex.ADMIN),
    VERMELHO   ("game.rank.vermelho",    LegacyHex.VERMELHO),
    OWNER      ("game.rank.owner",       LegacyHex.OWNER),
    ZW         ("game.rank.zw",          LegacyHex.ZW);

    private final String permission;
    private final String color;

    public boolean isAtLeast(RankType other) {
        return ordinal() >= other.ordinal();
    }

    /** Staff comeca em BUILDER (ordinal). */
    public boolean isStaff() {
        return isAtLeast(BUILDER);
    }

    public boolean isSuperAdmin() {
        return this == ZW || this == OWNER || this == DIRETOR || this == VERMELHO;
    }

    public java.util.List<String> getPermissionNodes() {
        java.util.List<String> nodes = new java.util.ArrayList<>();
        nodes.add(permission);
        if (isAtLeast(VIP)) {
            nodes.add("game.vip");
        }
        if (isStaff()) {
            nodes.add("game.staff");
            nodes.add("eventos.admin");
        }
        if (isAtLeast(ADMIN)) {
            nodes.add("game.admin");
            nodes.add("game.*");
            nodes.add("eventos.*");
        }
        if (isSuperAdmin()) {
            nodes.add("*");
            nodes.add("game.*");
            nodes.add("lobbyhost.*");
            nodes.add("proxyhost.*");
            nodes.add("auth.*");
            nodes.add("eventos.*");
            nodes.add("tag.*");
            nodes.add("medal.*");
        }
        return nodes;
    }

    public boolean grants(String key) {
        if (this == ZW) return key != null && !key.isEmpty();
        if (key == null || key.isEmpty()) return false;
        for (String node : getPermissionNodes()) {
            if (matchesPermission(node, key)) return true;
        }
        return false;
    }

    public static boolean matchesPermission(String held, String required) {
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
