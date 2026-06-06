package com.minecraft.eventos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minecraft.eventos.account.AccountController;
import com.minecraft.eventos.backend.data.AccountIdentityData;
import com.minecraft.eventos.backend.data.AccountData;
import com.minecraft.eventos.backend.database.HikariProvider;
import com.minecraft.eventos.backend.redis.RedisProvider;
import lombok.Getter;
import lombok.Setter;

import java.util.logging.Logger;

/**
 * Singleton estatico que centraliza acesso aos controllers/data layers.
 * Espelha {@code com.minecraft.core.Core} do Savage.
 *
 * <p>Inicializado em {@link EventosPlugin#onLoad()}.
 */
public final class Core {

    private Core() {}

    /** Gson compartilhado (mesma config do Savage: serializa nulls). */
    public static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    @Getter @Setter private static EventosPlugin javaPlugin;
    @Getter private static Logger logger;

    @Getter @Setter private static HikariProvider database;
    @Getter @Setter private static RedisProvider redis;

    @Getter @Setter private static AccountController accountController;
    @Getter @Setter private static AccountData accountData;
    @Getter @Setter private static AccountIdentityData accountIdentityData;

    public static void load(EventosPlugin plugin) {
        javaPlugin = plugin;
        logger = plugin.getLogger();
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
