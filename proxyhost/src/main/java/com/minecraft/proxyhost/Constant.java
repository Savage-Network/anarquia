package com.minecraft.proxyhost;

import java.util.UUID;

/**
 * Constantes globais (espelho de {@code com.minecraft.core.Constant} do Savage).
 *
 * <p>Compatibilidade: usa os mesmos nomes/valores das constantes do plugin {@code game},
 * o que garante interoperabilidade no banco de dados e nos canais Redis.
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

    public static final String SOURCE_DIR = System.getProperty("source_dir", "com.minecraft.proxyhost");

    public static final UUID DEFAULT_ID = new UUID(0, 0);

    /* ----- Cooldown keys (iguais ao game) ----- */
    public static final String CHAT_COOLDOWN_KEY = "chat-cooldown";
    public static final String TAG_CHANGE_COOLDOWN_KEY = "tag-change-cooldown";
    public static final String TELL_COOLDOWN_KEY = "tell-cooldown";

    /* ----- Redis channels (compartilhados) ----- */
    public static final String REDIS_ACCOUNT_UPDATE_CHANNEL = "account-update-channel";
    public static final String REDIS_ACCOUNT_RANK_UPDATE_CHANNEL = "account-rank-update-channel";
    public static final String REDIS_ACCOUNT_PUNISHMENTS_CHANNEL = "account-punishments-channel";
    public static final String REDIS_ACCOUNT_GLOBAL_MESSAGE_CHANNEL = "account-global-message-channel";
    public static final String REDIS_PROXY_TELL_CHANNEL = "proxy-tell-channel";
    public static final String REDIS_PROXY_STAFFCHAT_CHANNEL = "proxy-staffchat-channel";
    public static final String REDIS_PROXY_SETTINGS_CHANNEL = "proxy-settings-channel";

    public static final String[] REDIS_CHANNELS = new String[]{
            REDIS_ACCOUNT_UPDATE_CHANNEL,
            REDIS_ACCOUNT_RANK_UPDATE_CHANNEL,
            REDIS_ACCOUNT_PUNISHMENTS_CHANNEL,
            REDIS_ACCOUNT_GLOBAL_MESSAGE_CHANNEL
    };

    public static final String DATABASE_NAME = SERVER_NAME.toLowerCase();

    public static final long TAG_CHANGE_COOLDOWN_MS = 5_000L;
    public static final long CHAT_COOLDOWN_MS = 3_000L;
    public static final long TELL_COOLDOWN_MS = 1_500L;

    public static final String SERVER_STORE = "§6loja.savage.com.br";

    public static final String ACCOUNT_LOAD_FAILED_MESSAGE = "§cOcorreu um erro ao tentar carregar a sua conta!";

    public static final String BAN_TEMPLATE_MESSAGE =
            "§6§l" + SERVER_NAME.toUpperCase() + "\n\n"
                    + "§cA sua conta esta %s suspensa.\n"
                    + "§cMotivo: %s\n"
                    + "§cID: %s\n";
}
