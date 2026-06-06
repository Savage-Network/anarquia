package com.minecraft.lobbyhost;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minecraft.lobbyhost.account.AccountController;
import com.minecraft.lobbyhost.backend.data.AccountIdentityData;
import com.minecraft.lobbyhost.backend.data.AccountData;
import com.minecraft.lobbyhost.backend.data.ClanData;
import com.minecraft.lobbyhost.backend.data.NetworkStatsData;
import com.minecraft.lobbyhost.backend.data.NetworkPresenceData;
import com.minecraft.lobbyhost.backend.data.PunishmentData;
import com.minecraft.lobbyhost.backend.data.ReportData;
import com.minecraft.lobbyhost.backend.database.HikariProvider;
import com.minecraft.lobbyhost.backend.redis.RedisProvider;
import lombok.Getter;
import lombok.Setter;

import java.util.logging.Logger;

/**
 * Singleton estatico que centraliza acesso aos controllers/data layers.
 * Espelha {@code com.minecraft.core.Core} do Savage.
 *
 * <p>Inicializado em {@link LobbyHostPlugin#onLoad()}.
 */
public final class Core {

    private Core() {}

    /** Gson compartilhado (mesma config do Savage: serializa nulls). */
    public static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    @Getter @Setter private static LobbyHostPlugin javaPlugin;
    @Getter private static Logger logger;

    @Getter @Setter private static HikariProvider database;
    @Getter @Setter private static RedisProvider redis;

    @Getter @Setter private static AccountController accountController;
    @Getter @Setter private static AccountData accountData;
    @Getter @Setter private static AccountIdentityData accountIdentityData;
    @Getter @Setter private static ClanData clanData;
    @Getter @Setter private static NetworkStatsData networkStatsData;
    @Getter @Setter private static NetworkPresenceData networkPresenceData;
    @Getter @Setter private static ReportData reportData;
    @Getter @Setter private static PunishmentData punishmentData;

    public static void load(LobbyHostPlugin plugin) {
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
