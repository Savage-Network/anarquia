package com.minecraft.auth;

/** Valores fixos do auth (nao editaveis em config.yml). */
public final class AuthSettings {

    private AuthSettings() {}

    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int MIN_PASSWORD_LENGTH = 4;
    public static final int VALIDATION_HTTP_PORT = 0;

    public static final String POST_LOGIN_TARGET = "lobby";
    public static final int POST_LOGIN_DELAY_TICKS = 10;

    /** Apenas dev local via propriedade JVM: -Dsavage.dev.bypass-auth=true */
    public static final boolean DEV_BYPASS_AUTH =
            Boolean.parseBoolean(System.getProperty("savage.dev.bypass-auth", "false"));
}
