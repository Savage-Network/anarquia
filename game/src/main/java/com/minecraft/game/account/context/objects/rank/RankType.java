package com.minecraft.game.account.context.objects.rank;

import com.minecraft.game.account.context.objects.tag.LegacyHex;
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
    MODPLUS    ("game.rank.modplus",     LegacyHex.MOD_PLUS),
    ADMIN      ("game.rank.admin",       LegacyHex.ADMIN),
    DIRETOR    ("game.rank.diretor",     LegacyHex.ADMIN),
    VERMELHO   ("game.rank.vermelho",    LegacyHex.VERMELHO),
    OWNER      ("game.rank.owner",       LegacyHex.OWNER),
    ZW         ("game.rank.zw",          LegacyHex.ZW);

    /** Cor da tag ZW (#9D0000) — rank e tag usam o mesmo valor. */
    public static final String ZW_COLOR = ZW.color;

    private final String permission;
    private final String color;

    public boolean isAtLeast(RankType other) {
        return ordinal() >= other.ordinal();
    }

    /** Staff comeca em BUILDER (ordinal). */
    public boolean isStaff() {
        return isAtLeast(BUILDER);
    }

    /** Rank com acesso total (wildcard *). */
    public boolean isSuperAdmin() {
        return this == ZW || this == OWNER || this == DIRETOR || this == VERMELHO;
    }

    /** Nome colorido para scoreboard/tab (ZW usa bold como a tag). */
    public String formatScoreboard() {
        if (this == ZW) {
            return color + "§lZW";
        }
        return color + name();
    }

    /** Nick no chat com a cor do rank (ZW bold como tab). */
    public String formatChatNickname(String nickname) {
        if (nickname == null || nickname.isEmpty()) return "";
        if (this == ZW) {
            return color + "§l" + nickname;
        }
        return color + nickname;
    }

    /**
     * Permissoes Bukkit sincronizadas via {@link com.minecraft.game.bukkit.manager.PermissionManager}.
     * Hierarquia: MEMBER → VIP → BUILDER → HELPER → MOD → ADMIN → OWNER.
     */
    public java.util.List<String> getPermissionNodes() {
        if (this == ZW) {
            return zwPermissionNodes();
        }

        java.util.List<String> nodes = new java.util.ArrayList<>();
        nodes.add(permission);
        nodes.add("game.report");
        nodes.add("game.shop.create");

        if (isAtLeast(VIP)) {
            nodes.add("game.vip");
            nodes.add("game.kit.vip");
        }
        if (isStaff()) {
            nodes.add("game.staff");
            nodes.add("game.go");
            nodes.add("game.vanish");
            nodes.add("game.vanish.see");
            nodes.add("game.staffchat");
            nodes.add("game.kit.staff");
            nodes.add("game.chat.mod");
        }
        if (isAtLeast(HELPER)) {
            nodes.add("game.reports");
            nodes.add("game.punish");
            nodes.add("game.kick");
        }
        if (isAtLeast(MOD)) {
            nodes.add("game.unpunish");
            nodes.add("game.cban");
            nodes.add("game.ban");
        }
        if (isAtLeast(ADMIN)) {
            nodes.add("game.admin");
            nodes.add("game.*");
            nodes.add("game.money.give");
            nodes.add("game.money.take");
            nodes.add("game.shop.admin");
        }
        if (isSuperAdmin()) {
            nodes.add("*");
        }
        return nodes;
    }

    private static java.util.List<String> zwPermissionNodes() {
        java.util.List<String> nodes = new java.util.ArrayList<>();
        nodes.add("game.rank.zw");
        nodes.add("*");
        nodes.add("game.*");
        nodes.add("lobbyhost.*");
        nodes.add("proxyhost.*");
        nodes.add("auth.*");
        nodes.add("eventos.*");
        nodes.add("tag.*");
        nodes.add("medal.*");
        nodes.add("game.report");
        nodes.add("game.shop.create");
        nodes.add("game.shop.admin");
        nodes.add("game.vip");
        nodes.add("game.kit.vip");
        nodes.add("game.kit.staff");
        nodes.add("game.staff");
        nodes.add("game.go");
        nodes.add("game.vanish");
        nodes.add("game.vanish.see");
        nodes.add("game.staffchat");
        nodes.add("game.chat.mod");
        nodes.add("game.reports");
        nodes.add("game.punish");
        nodes.add("game.kick");
        nodes.add("game.unpunish");
        nodes.add("game.cban");
        nodes.add("game.ban");
        nodes.add("game.admin");
        nodes.add("lobbyhost.admin");
        nodes.add("game.money.give");
        nodes.add("game.money.take");
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
