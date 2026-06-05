package com.minecraft.auth;

import com.minecraft.auth.config.AuthConfig;

/** @deprecated Use {@link AuthConfig}. Mantido para compatibilidade interna. */
@Deprecated
public final class AuthSettings {

    private AuthSettings() {}

    public static int getMaxLoginAttempts() {
        return AuthConfig.get() != null ? AuthConfig.get().getMaxLoginAttempts() : 5;
    }

    public static int getMinPasswordLength() {
        return AuthConfig.get() != null ? AuthConfig.get().getMinPasswordLength() : 4;
    }

    public static int getPostLoginDelayTicks() {
        return AuthConfig.get() != null ? AuthConfig.get().getPostLoginDelayTicks() : 10;
    }

    public static String getPostLoginTarget() {
        return AuthConfig.get() != null ? AuthConfig.get().getPostLoginTarget() : "lobby";
    }

    public static boolean isDevBypassAuth() {
        return AuthConfig.get() != null && AuthConfig.get().isDevBypassAuth();
    }

    /** Compat legado. */
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int MIN_PASSWORD_LENGTH = 4;
    public static final int POST_LOGIN_DELAY_TICKS = 10;
    public static final String POST_LOGIN_TARGET = "lobby";
    public static final int VALIDATION_HTTP_PORT = 0;
    public static final boolean DEV_BYPASS_AUTH =
            Boolean.parseBoolean(System.getProperty("savage.dev.bypass-auth", "false"));
}
