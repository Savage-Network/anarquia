package com.minecraft.proxyhost;

/** Valores fixos do proxy (nao editaveis em config.yml). */
public final class ProxySettings {

    private ProxySettings() {}

    public static final String STAFFCHAT_PREFIX = "&6[STAFF] ";
    public static final long TELL_COOLDOWN_MS = 1_500L;
    public static final int MOTD_DEFAULT_MAX_DISPLAY = 100;
    public static final String MAINTENANCE_DEFAULT_MIN_RANK = "HELPER";

    public static final String SERVER_ADDRESS = "savagemc.net";
    public static final String SERVER_STORE_URL = "loja.savagemc.net";
    public static final String SERVER_STORE = "§6" + SERVER_STORE_URL;
}
