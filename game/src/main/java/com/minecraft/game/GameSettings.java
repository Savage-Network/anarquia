package com.minecraft.game;

import java.util.List;

/** Valores fixos do servidor semi-anarquia (nao editaveis em config.yml). */
public final class GameSettings {

    private GameSettings() {}

    public static final String DISPLAY_NAME = "Semi Anarquia";
    public static final String SERVER_MODE = "GAME";

    public static final String SIDEBAR_TITLE = "§4§lSEMI ANARQUIA";
    public static final long SIDEBAR_UPDATE_TICKS = 20L;
    public static final long TABLIST_UPDATE_TICKS = 40L;

    public static final List<String> TAB_HEADER = List.of("§6§lSAVAGE");
    public static final List<String> TAB_FOOTER = List.of("§7{online} online §8• §7savagemc.net");

    public static final int SPAWN_PROTECTION_RADIUS = 50;
    public static final int COMBAT_TAG_SECONDS = 15;
    public static final int STARTING_BALANCE = 1000;
    public static final String CURRENCY_SYMBOL = "$";

    public static final String SPAWN_WORLD = "world";
    public static final double SPAWN_X = 0.5;
    public static final double SPAWN_Y = 64.0;
    public static final double SPAWN_Z = 0.5;

    public static final int KILL_REWARD_BASE = 200;
    public static final int KILL_STREAK_BONUS = 50;
    public static final int MAX_KILL_STREAK_BONUS = 200;
    public static final int HOME_COOLDOWN_SECONDS = 30;
    public static final int HOME_COOLDOWN_VIP_SECONDS = 15;
    public static final int SPAWN_COOLDOWN_SECONDS = 30;
    public static final int WARP_COOLDOWN_SECONDS = 5;
    public static final int BOUNTY_MIN = 500;
    public static final int BOUNTY_TAX_PERCENT = 10;

    public static final int AUCTION_MAX_LISTINGS = 5;
    public static final int AUCTION_DURATION_HOURS = 48;
    public static final int AUCTION_TAX_PERCENT = 5;
    public static final int AUCTION_LISTING_FEE = 50;

    public static final boolean SIGN_SHOPS_ENABLED = false;
    public static final int SIGN_SHOP_MAX_PRICE = 1_000_000;
    public static final int SIGN_SHOP_TAX_PERCENT = 2;

    /** Nametag flutuante (estilo display groups / UnlimitedNameTags). */
    public static final double NAMETAG_BASE_Y = 2.05;
    public static final double NAMETAG_LINE_SPACING = 0.28;
    public static final float NAMETAG_TEXT_SCALE = 1.05f;
    public static final int NAMETAG_BG_ALPHA = 72;
    public static final long NAMETAG_FOLLOW_TICKS = 2L;
}
