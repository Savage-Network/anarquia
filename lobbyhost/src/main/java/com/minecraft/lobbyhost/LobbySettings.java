package com.minecraft.lobbyhost;

import org.bukkit.Material;

import java.util.List;

/** Valores fixos do hub (nao editaveis em config.yml). */
public final class LobbySettings {

    private LobbySettings() {}

    public static final String ARCHITECT_FILE = "plugins/lobbyhost/architect.json";
    public static final String DISPLAY_NAME = "Hub Principal";
    public static final String SERVER_MODE = "HUB";

    public static final String NAVIGATION_TITLE_MINI =
            "<!italic><gradient:#FF8C42:#FFD166><bold>Onde jogar?</bold></gradient>";
    public static final String SIDEBAR_TITLE_MINI =
            "<!italic><gradient:#38BDF8:#818CF8><bold>LOBBY</bold></gradient>";
    public static final long SIDEBAR_UPDATE_TICKS = 20L;
    public static final long TABLIST_UPDATE_TICKS = 40L;

    public static final String SERVER_ADDRESS = "savagemc.net";
    public static final String SERVER_STORE_URL = "loja.savagemc.net";
    public static final String SERVER_STORE = "§6" + SERVER_STORE_URL;

    public static final List<String> TAB_HEADER = List.of(
            "<!italic><gradient:#FF8C42:#FFD166><bold>SAVAGE</bold></gradient>",
            "<!italic><#94A3B8>" + SERVER_ADDRESS);
    public static final List<String> TAB_FOOTER = List.of(
            "<!italic><#94A3B8>{online} online <#64748B>• <#F1F5F9>" + SERVER_ADDRESS,
            "<!italic><#94A3B8>Loja <#FFD166>" + SERVER_STORE_URL);

    public static final String DEFAULT_SPAWN_WORLD = "world";
    public static final double DEFAULT_SPAWN_X = 0.5;
    public static final double DEFAULT_SPAWN_Y = 100;
    public static final double DEFAULT_SPAWN_Z = 0.5;

    public static final String DISCORD_URL = "discord.gg/savagemc";
    public static final long VISIBILITY_COOLDOWN_SECONDS = 2;

    public static final String LOBBY_CHAT_PREFIX = "§8[Lobby]§r ";
    public static final String STAFFCHAT_PREFIX = "§6[Staff]§r ";
    public static final String SERVER_CHAT_ID = "lobby";

    public static final int CHAT_MAX_LENGTH = 256;
    public static final long CHAT_REPEAT_WINDOW_MS = 6_000L;
    public static final int CHAT_MAX_REPEAT = 3;
    /** Prefixos que enviam direto ao staff chat (staff). */
    public static final String[] CHAT_STAFF_PREFIXES = {"!", "@"};
    public static final String CHAT_MENTION_COLOR = "§e";
    public static final int PARTY_MAX_SIZE = 8;
    public static final long NETWORK_POLL_TICKS = 40L;
    public static final long LEADERBOARD_REFRESH_TICKS = 20L * 60L;

    /**
     * Modos no menu da bussola.
     *
     * @param bungeeServer nome no canal BungeeCord/Velocity
     * @param heartbeatId id em {@link com.minecraft.lobbyhost.Constant#REDIS_NETWORK_ONLINE_KEY_PREFIX}
     */
    public record NetworkMode(String id, String bungeeServer, String heartbeatId,
                              String displayName, Material material, int slot) {}

    public static final List<NetworkMode> NETWORK_MODES = List.of(
            new NetworkMode("semi-anarquia", "game", "game",
                    "<!italic><gradient:#EF4444:#F87171><bold>Semi Anarquia</bold></gradient>",
                    Material.DIAMOND_SWORD, 11),
            new NetworkMode("eventos", "eventos", "eventos",
                    "<!italic><gradient:#C084FC:#F472B6><bold>Eventos</bold></gradient>",
                    Material.FIREWORK_ROCKET, 15)
    );
}
