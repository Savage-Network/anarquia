package com.minecraft.lobbyhost;

import java.util.List;

/** Valores fixos do hub (nao editaveis em config.yml). */
public final class LobbySettings {

    private LobbySettings() {}

    public static final String ARCHITECT_FILE = "plugins/lobbyhost/architect.json";
    public static final String DISPLAY_NAME = "Hub Principal";
    public static final String SERVER_MODE = "HUB";

    public static final String NAVIGATION_TITLE = "§6§lOnde jogar?";
    public static final String SIDEBAR_TITLE = "§6§lSAVAGE";
    public static final long SIDEBAR_UPDATE_TICKS = 20L;
    public static final long TABLIST_UPDATE_TICKS = 40L;

    public static final List<String> TAB_HEADER = List.of("§6§lSAVAGE");
    public static final List<String> TAB_FOOTER = List.of("§7{online} online §8• §7savagemc.net");

    public static final String DEFAULT_SPAWN_WORLD = "world";
    public static final double DEFAULT_SPAWN_X = 0.5;
    public static final double DEFAULT_SPAWN_Y = 100;
    public static final double DEFAULT_SPAWN_Z = 0.5;

    public static final String DISCORD_URL = "discord.gg/savagemc";
    public static final long VISIBILITY_COOLDOWN_SECONDS = 2;

    public static final double NAMETAG_BASE_Y = 2.05;
    public static final double NAMETAG_LINE_SPACING = 0.28;
    public static final float NAMETAG_TEXT_SCALE = 1.05f;
    public static final int NAMETAG_BG_ALPHA = 72;
    public static final long NAMETAG_FOLLOW_TICKS = 2L;
}
