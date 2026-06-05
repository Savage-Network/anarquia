package com.minecraft.eventos.account.context.objects.rank;

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
    OWNER      ("game.rank.owner",       "§4"),
    ZW         ("game.rank.zw",          "§x§9§D§0§0§0§0");

    private final String permission;
    private final String color;

    public boolean isAtLeast(RankType other) {
        return ordinal() >= other.ordinal();
    }

    /** Staff comeca em BUILDER (ordinal). */
    public boolean isStaff() {
        return isAtLeast(BUILDER);
    }
}
