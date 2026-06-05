package com.minecraft.auth.config;

import com.minecraft.auth.AuthPlugin;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

/** Configuração do servidor de autenticação (config.yml → auth). */
public final class AuthConfig {

    @Getter private static AuthConfig instance;

    private final int maxLoginAttempts;
    private final int minPasswordLength;
    private final int postLoginDelayTicks;
    private final String postLoginTarget;
    private final int reminderIntervalTicks;
    private final boolean devBypassAuth;

    private AuthConfig(FileConfiguration config) {
        var section = config.getConfigurationSection("auth");
        maxLoginAttempts = section != null ? section.getInt("max-login-attempts", 5) : 5;
        minPasswordLength = section != null ? section.getInt("min-password-length", 4) : 4;
        postLoginDelayTicks = section != null ? section.getInt("post-login-delay-ticks", 10) : 10;
        postLoginTarget = section != null ? section.getString("post-login-target", "lobby") : "lobby";
        reminderIntervalTicks = section != null ? section.getInt("reminder-interval-ticks", 200) : 200;
        devBypassAuth = section != null && section.getBoolean("dev-bypass",
                Boolean.parseBoolean(System.getProperty("savage.dev.bypass-auth", "false")));
    }

    public static void load(AuthPlugin plugin) {
        instance = new AuthConfig(plugin.getConfig());
    }

    public static AuthConfig get() {
        return instance;
    }

    public int getMaxLoginAttempts() { return maxLoginAttempts; }
    public int getMinPasswordLength() { return minPasswordLength; }
    public int getPostLoginDelayTicks() { return postLoginDelayTicks; }
    public String getPostLoginTarget() { return postLoginTarget; }
    public int getReminderIntervalTicks() { return reminderIntervalTicks; }
    public boolean isDevBypassAuth() { return devBypassAuth; }
}
