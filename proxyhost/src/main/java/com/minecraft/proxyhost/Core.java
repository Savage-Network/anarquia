package com.minecraft.proxyhost;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minecraft.proxyhost.account.AccountController;
import com.minecraft.proxyhost.backend.data.AccountData;
import com.minecraft.proxyhost.backend.database.HikariProvider;
import com.minecraft.proxyhost.backend.redis.RedisProvider;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;

/**
 * Singleton estatico que centraliza acesso aos controllers/data layers.
 * Espelha {@code com.minecraft.core.Core} do Savage, adaptado para Velocity.
 */
public final class Core {

    private Core() {}

    public static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    @Getter @Setter private static ProxyHostPlugin plugin;
    @Getter @Setter private static ProxyServer proxy;
    @Getter @Setter private static Logger logger;

    @Getter @Setter private static HikariProvider database;
    @Getter @Setter private static RedisProvider redis;

    @Getter @Setter private static AccountController accountController;
    @Getter @Setter private static AccountData accountData;

    public static void load(ProxyHostPlugin pluginInstance, ProxyServer proxyServer, Logger log) {
        plugin = pluginInstance;
        proxy = proxyServer;
        logger = log;
    }

    public static void unload() {
        if (accountController != null) accountController.unloadAll();
        if (database != null) database.shutdown();
        if (redis != null) redis.shutdown();
    }

    public static void message(String... messages) {
        if (logger == null) return;
        for (String m : messages) logger.info(m);
    }
}
