package com.minecraft.game;

import java.util.List;

/** Valores fixos do servidor semi-anarquia (nao editaveis em config.yml). */
public final class GameSettings {

    private GameSettings() {}

    public static final String DISPLAY_NAME = "SavageMC";
    public static final String SERVER_MODE = "GAME";

    public static final String SIDEBAR_TITLE = "§6§lSavageMC";
    public static final long SIDEBAR_UPDATE_TICKS = 20L;
    public static final long TABLIST_UPDATE_TICKS = 40L;
    public static final long NETWORK_POLL_TICKS = 40L;

    public static final String SERVER_ADDRESS = "savagemc.net";
    public static final String SERVER_STORE_URL = "loja.savagemc.net";
    public static final String SERVER_STORE = "§6" + SERVER_STORE_URL;

    public static final List<String> TAB_HEADER = List.of(
            " ",
            "<!italic><gradient:#FF8C42:#FFD166><bold>SavageMC</bold></gradient>",
            "<!italic><gradient:#F97316:#FBBF24><bold>Semi-Anarquia</bold></gradient>",
            "<!italic><#94A3B8>" + SERVER_ADDRESS,
            " ");
    public static final List<String> TAB_FOOTER = List.of(
            " ",
            "<!italic><#FFD166><bold>" + SERVER_STORE_URL + "</bold>",
            "<!italic><#64748B>Online: <!italic><#4ADE80>{online}",
            "<!italic><#94A3B8>" + SERVER_ADDRESS,
            " ");

    /** Raio da zona da loja (spawn protegido — sem build/PvP). */
    public static final int SPAWN_PROTECTION_RADIUS = 45;
    public static final int COMBAT_TAG_SECONDS = 15;
    public static final int STARTING_BALANCE = 1000;
    public static final String CURRENCY_SYMBOL = "$";

    public static final String GAME_CHAT_PREFIX = "";
    public static final String STAFFCHAT_PREFIX = "§6[Staff]§r ";
    public static final String ANTICHEAT_ALERT_PREFIX = "§c[ANTICHEAT] ";
    public static final boolean ANTICHEAT_ENABLED = true;
    public static final boolean ANTICHEAT_ALERT_STAFF = true;
    public static final String SERVER_CHAT_ID = "game";

    public static final int CHAT_MAX_LENGTH = 256;
    public static final long CHAT_REPEAT_WINDOW_MS = 6_000L;
    public static final int CHAT_MAX_REPEAT = 3;
    public static final String[] CHAT_STAFF_PREFIXES = {"!", "@"};
    public static final String CHAT_MENTION_COLOR = "§e";

    /** Spawn / loja no mapa {@code spawn}. */
    public static final String SPAWN_WORLD = "spawn";
    /** Mapa principal PvP — destino do {@code /rtp}. */
    public static final String RTP_WORLD = "world";
    public static final double SPAWN_X = -18.0;
    public static final double SPAWN_Y = 10.0;
    public static final double SPAWN_Z = -116.5;
    public static final float SPAWN_YAW = -91.25f;
    public static final float SPAWN_PITCH = 2.93f;

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

    public static final boolean SIGN_SHOPS_ENABLED = true;
    public static final int SIGN_SHOP_MAX_PRICE = 1_000_000;
    public static final int SIGN_SHOP_TAX_PERCENT = 2;

    public static final long LEADERBOARD_REFRESH_TICKS = 20L * 60L;

}
