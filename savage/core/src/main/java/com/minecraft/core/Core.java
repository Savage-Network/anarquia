package com.minecraft.core;

import com.minecraft.core.api.mojang.MojangApi;
import com.minecraft.core.backend.data.list.api.*;
import com.minecraft.core.backend.data.list.server.RouteData;
import com.minecraft.core.backend.data.list.server.SeasonData;
import com.minecraft.core.backend.data.list.server.ServerData;
import com.minecraft.core.backend.data.list.user.AccountData;
import com.minecraft.core.backend.data.list.user.arcade.list.*;
import com.minecraft.core.backend.database.DatabaseCredentials;
import com.minecraft.core.backend.database.postgresql.PostgresDatabase;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import com.minecraft.core.controller.list.AccountController;
import com.minecraft.core.controller.list.CollectibleController;
import com.minecraft.core.controller.list.JoinMessageController;
import com.minecraft.core.controller.list.MemberController;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.type.ServerType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.minecraft.core.account.context.objects.rank.Rank;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.assignment.Assignment;
import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Logger;

public class Core {

    @Getter @Setter private static Platform platform;
    @Getter @Setter private static Logger logger;
    @Getter @Setter private static JavaPlugin javaPlugin;
    @Getter @Setter private static PostgresDatabase postgres;
    @Getter @Setter private static RedisDatabase redis;
    @Getter @Setter private static ServerData serverData;
    @Getter @Setter private static SeasonData seasonData;
    @Getter @Setter private static ReportData reportData;
    @Getter @Setter private static AccountData accountData;
    @Getter @Setter private static RouteData routeData;
    @Getter @Setter private static ClanData clanData;
    @Getter @Setter private static ArcadeData arcadeData;
    @Getter @Setter private static DuelsData duelsData;
    @Getter @Setter private static PvPData pvpData;
    @Getter @Setter private static BedWarsData bedWarsData;
    @Getter @Setter private static EggWarsData eggWarsData;
    @Getter @Setter private static SkyWarsData skyWarsData;
    @Getter @Setter private static TheBridgeData theBridgeData;
    @Getter @Setter private static PartyData partyData;
    @Getter @Setter private static PunishmentData punishmentData;
    @Getter @Setter private static RedirectData redirectData;
    @Getter @Setter private static ReconnectData reconnectData;
    @Getter @Setter private static SkinCacheData skinCacheData;
    @Getter @Setter private static com.minecraft.core.backend.data.list.code.RedeemCodeData redeemCodeData;
    @Getter @Setter private static ServerType serverType = ServerType.BUNGEE;
    @Getter @Setter private static int serverId = 0, serverPort = 25565;
    @Getter private final static AccountController accountController = new AccountController();
    @Getter private final static MemberController memberController = new MemberController();
    @Getter private final static CollectibleController collectibleController = new CollectibleController();
    @Getter private final static JoinMessageController joinMessageController = new JoinMessageController();

    public static Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder();
        Gson base = builder.create();
        GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new com.google.gson.TypeAdapterFactory() {
                @Override
                public <T> TypeAdapter<T> create(Gson gson, com.google.gson.reflect.TypeToken<T> type) {
                    if (!Rank.class.isAssignableFrom(type.getRawType())) return null;
                    TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
                    return new TypeAdapter<T>() {
                        @Override
                        public void write(JsonWriter out, T value) throws java.io.IOException {
                            delegate.write(out, value);
                        }
                        @SuppressWarnings("unchecked")
                        @Override
                        public T read(JsonReader in) throws java.io.IOException {
                            if (in.peek() == JsonToken.NULL) { in.nextNull(); return (T) Rank.builder().build(); }
                            T result = delegate.read(in);
                            Rank rank = (Rank) result;
                            if (rank != null && rank.getType() == null) {
                                return (T) Rank.builder()
                                        .type(RankType.MEMBER)
                                        .assignment(rank.getAssignment() != null ? rank.getAssignment() : Assignment.AUTO)
                                        .build();
                            }
                            return result;
                        }
                    };
                }
            })
            .create();
    }
    public static JsonParser PARSER = new JsonParser();
    public static Random RANDOM = new Random();
    public static MojangApi MOJANG_API = new MojangApi();

    public static Server getLocalServer() {
        return serverData.local();
    }

    public static boolean isUntouchable(UUID id) {
        return Constant.DEVELOPERS_IDS.contains(id);
    }

    public static void load(Platform platform, ServerData serverData, Logger logger) {
        logger.info("Iniciando Core...");

        setPlatform(platform);
        setLogger(logger);

        TimeZone.setDefault(TimeZone.getTimeZone("GMT-3"));

        PostgresDatabase postgres = new PostgresDatabase(DatabaseCredentials.builder()
                .host("198.1.195.80")
                .port(5432)
                .database("savagemc")
                .user("savagemc")
                .password("savagemcnaochorax")
                .build());
        postgres.load();

        RedisDatabase redis = new RedisDatabase(DatabaseCredentials.builder()
                .host("198.1.195.80")
                .port(6379)
                .build());
        redis.load();

        setPostgres(postgres);
        setRedis(redis);

        setServerData(serverData);
        setSeasonData(new SeasonData(postgres));

        setRouteData(new RouteData(redis));
        setArcadeData(new ArcadeData(redis));
        setReportData(new ReportData(redis));
        setClanData(new ClanData(postgres, redis));
        setPartyData(new PartyData(redis));
        setPunishmentData(new PunishmentData(postgres));
        setRedirectData(new RedirectData(redis));
        setReconnectData(new ReconnectData(redis));
        setSkinCacheData(new SkinCacheData(redis));
        setRedeemCodeData(new com.minecraft.core.backend.data.list.code.RedeemCodeData(postgres));

        setAccountData(new AccountData(postgres, redis));

        setDuelsData(new DuelsData(postgres, redis));
        setPvpData(new PvPData(postgres, redis));
        setBedWarsData(new BedWarsData(postgres, redis));
        setEggWarsData(new EggWarsData(postgres, redis));
        setSkyWarsData(new SkyWarsData(postgres, redis));
        setTheBridgeData(new TheBridgeData(postgres, redis));

        logger.info("Core iniciado com sucesso.");
    }

    public static void unload() {
        logger.info("Cancelando serviços...");

        serverData.stop();

        postgres.unload();
        redis.unload();

        logger.info("Serviços cancelados com sucesso.");
    }

    public static void scan(String title, Instant started) {
        logger.info("[Scan/" + title + "] Examined with " + Duration.between(started, Instant.now()).toMillis() + " ms!");
    }

    public static void message(String... texts) {
        for (String text : texts)
            logger.info(text);
    }
}
