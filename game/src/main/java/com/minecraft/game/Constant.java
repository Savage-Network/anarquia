package com.minecraft.game;

import java.util.UUID;

/**
 * Constantes globais (espelho de {@code com.minecraft.core.Constant} do Savage).
 *
 * <p>Mantemos o mesmo padrao: {@link #SOURCE_DIR} alimenta o scan reflection
 * de {@link com.minecraft.game.bukkit.command.structure.BukkitCommandHandler}
 * e {@link com.minecraft.game.bukkit.listener.handler.ListenerHandler}.
 */
public final class Constant {

    private Constant() {}

    public static final String SERVER_NAME = System.getProperty("server_name", "Savage");

    public static final String ARROW_SYMBOL = "»";
    public static final String ARROW_ALT_SYMBOL = "‣";
    public static final String SQUARE_SYMBOL = "▪";

    public static final String SERVER_COLOR = "§6§l";
    public static final String SERVER_TITLE = SERVER_COLOR + SERVER_NAME.toUpperCase();
    public static final String SERVER_PREFIX = SERVER_TITLE + " §7" + ARROW_SYMBOL + " §r";

    /** Pacote raiz para os scans reflection (commands/listeners). */
    public static final String SOURCE_DIR = System.getProperty("source_dir", "com.minecraft.game");

    public static final UUID DEFAULT_ID = new UUID(0, 0);

    /* ----- Cooldown keys ----- */
    public static final String CHAT_COOLDOWN_KEY = "chat-cooldown";
    public static final String TAG_CHANGE_COOLDOWN_KEY = "tag-change-cooldown";
    public static final String SKIN_CHANGE_COOLDOWN_KEY = "skin-change-cooldown";
    public static final String NICK_CHANGE_COOLDOWN_KEY = "nick-change-cooldown";
    public static final String REPORT_SEND_COOLDOWN_KEY = "report-send-cooldown";
    public static final String PARTY_WARN_COOLDOWN_KEY = "party-warn-cooldown";
    public static final String PLAYERS_VISIBILITY_COOLDOWN_KEY = "players-visibility-cooldown";

    /* ----- Redis channels ----- */
    public static final String REDIS_ACCOUNT_UPDATE_CHANNEL = "account-update-channel";
    public static final String REDIS_ACCOUNT_RANK_UPDATE_CHANNEL = "account-rank-update-channel";
    public static final String REDIS_ACCOUNT_PUNISHMENTS_CHANNEL = "account-punishments-channel";
    public static final String REDIS_ACCOUNT_GLOBAL_MESSAGE_CHANNEL = "account-global-message-channel";

    public static final String[] REDIS_CHANNELS = new String[]{
            REDIS_ACCOUNT_UPDATE_CHANNEL,
            REDIS_ACCOUNT_RANK_UPDATE_CHANNEL,
            REDIS_ACCOUNT_PUNISHMENTS_CHANNEL,
            REDIS_ACCOUNT_GLOBAL_MESSAGE_CHANNEL
    };

    /* ----- Banco ----- */
    public static final String DATABASE_NAME = SERVER_NAME.toLowerCase();

    /* ----- Sistema de tags ----- */
    /** Cooldown padrao do {@code /tag} (em ms). */
    public static final long TAG_CHANGE_COOLDOWN_MS = 5_000L;

    /** Cooldown do chat para nao-VIP (em ms). */
    public static final long CHAT_COOLDOWN_MS = 3_000L;

    /** URL/menu da loja exibido em /medal vazio. */
    public static final String SERVER_STORE = "§6loja.savage.com.br";

    /* ----- Clan ----- */
    public static final String CLAN_CHAT_PREFIX = "§8[§3Cla§8] §r";
    public static final int CLAN_MAX_MEMBERS = 15;
    public static final int CLAN_NAME_MIN = 3;
    public static final int CLAN_NAME_MAX = 12;
    public static final int CLAN_TAG_MIN = 2;
    public static final int CLAN_TAG_MAX = 5;

    /* ----- Mensagens ----- */
    public static final String ACCOUNT_LOAD_FAILED_MESSAGE = "§cOcorreu um erro ao tentar carregar a sua conta!";

    public static final String BAN_TEMPLATE_MESSAGE =
            "§6§l" + SERVER_NAME.toUpperCase() + "\n\n"
                    + "§cA sua conta esta %s suspensa.\n"
                    + "§cMotivo: %s\n"
                    + "§cID: %s\n";
}
