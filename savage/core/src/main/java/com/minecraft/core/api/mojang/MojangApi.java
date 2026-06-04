package com.minecraft.core.api.mojang;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.properties.Property;
import com.minecraft.core.Core;
import com.minecraft.core.api.mojang.exception.MojangException;
import com.minecraft.core.util.list.Validator;
import com.minecraft.core.util.list.http.HttpRequest;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MojangApi {

    private static final int TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 15000;
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; Lands/1.0)";
    private static final List<String> APIS_URL = Arrays.asList(
            "https://api.mojang.com/users/profiles/minecraft/%s",
            "https://api.minetools.eu/uuid/%s",
            "https://api.mcuuid.com/v1/uuid/%s"
    );

    private final LoadingCache<String, Optional<UUID>> uuidCache;
    private final LoadingCache<UUID, Optional<String>> nameCache;
    private final LoadingCache<UUID, Optional<Property>> texturesCache;

    public MojangApi() {
        uuidCache = createCache(new CacheLoader<String, Optional<UUID>>() {
            @Override
            public Optional<UUID> load(String key) throws Exception {
                return fetchUUID(key);
            }
        });

        nameCache = createCache(new CacheLoader<UUID, Optional<String>>() {
            @Override
            public Optional<String> load(UUID key) throws Exception {
                return fetchName(key);
            }
        });

        texturesCache = createCache(new CacheLoader<UUID, Optional<Property>>() {
            @Override
            public Optional<Property> load(UUID key) throws Exception {
                return fetchTextures(key);
            }
        });
    }

    private <K, V> LoadingCache<K, V> createCache(CacheLoader<K, V> loader) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build(loader);
    }


    private Optional<UUID> fetchUUID(String name) {
        return retry(() -> requestUUID(name), 3);
    }

    private Optional<String> fetchName(UUID uuid) {
        return retry(() -> requestName(uuid), 3);
    }

    private Optional<Property> fetchTextures(UUID uuid) {
        return retry(() -> requestTextures(uuid), 3);
    }

    private <T> Optional<T> retry(CallableWithException<T> action, int retries) {
        for (int i = 0; i < retries; i++) {
            try {
                return Optional.ofNullable(action.call());
            } catch (Exception e) {
                if (i == retries - 1) {
                    Core.getLogger().log(Level.WARNING, "Erro ao realizar tentativa", e);
                }
            }
        }
        return Optional.empty();
    }

    private UUID requestUUID(String name) throws Exception {
        HttpRequest request = HttpRequest.get(String.format(APIS_URL.get(0), name))
                .connectTimeout(CONNECT_TIMEOUT)
                .readTimeout(TIMEOUT)
                .userAgent(USER_AGENT)
                .acceptJson();

        if (request.ok()) {
            JsonObject object = Core.PARSER.parse(request.reader()).getAsJsonObject();
            return parseUUID(object.get("id").getAsString());
        } else if (request.noContent()) {
            return null;
        }
        throw new MojangException(MojangException.ErrorType.UNKNOWN);
    }

    private String requestName(UUID uuid) throws Exception {
        String rawUUID = uuid.toString().replace("-", "");
        HttpRequest request = HttpRequest.get("https://api.mojang.com/user/profiles/" + rawUUID + "/names")
                .connectTimeout(TIMEOUT)
                .readTimeout(TIMEOUT)
                .userAgent(USER_AGENT)
                .acceptJson();

        if (request.ok()) {
            JsonArray jsonArray = Core.PARSER.parse(request.reader()).getAsJsonArray();
            JsonObject jsonObject = jsonArray.get(jsonArray.size() - 1).getAsJsonObject();
            return jsonObject.get("name").getAsString();
        } else if (request.noContent()) {
            return null;
        }
        throw new MojangException(MojangException.ErrorType.UNKNOWN);
    }

    private Property requestTextures(UUID uuid) throws Exception {
        HttpRequest request = HttpRequest.get("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false")
                .connectTimeout(TIMEOUT)
                .readTimeout(TIMEOUT)
                .userAgent(USER_AGENT)
                .acceptJson();

        if (request.ok()) {
            JsonObject json = Core.PARSER.parse(request.reader()).getAsJsonObject();
            JsonObject prop = json.getAsJsonArray("properties").get(0).getAsJsonObject();
            return Core.GSON.fromJson(prop, Property.class);
        }
        return null;
    }

    private UUID parseUUID(String id) {
        if (id != null && !id.isEmpty()) {
            return UUID.fromString(id.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        }
        return null;
    }

    public ResponseCode isPremium(String name) {
        return retry(() -> checkPremium(name), 3).orElse(ResponseCode.UNKNOWN);
    }

    private ResponseCode checkPremium(String name) throws Exception {
        for (String apiUrl : APIS_URL) {
            try {
                HttpRequest request = HttpRequest.get(String.format(apiUrl, name))
                        .connectTimeout(TIMEOUT)
                        .readTimeout(TIMEOUT)
                        .userAgent(USER_AGENT)
                        .acceptJson();

                int code = request.code();

                if (code == 404) return ResponseCode.FAIL;
                if (code == 200) return ResponseCode.DONE;

                Core.getLogger().warning("Erro inesperado ao verificar jogador " + name + ": Código de resposta " + code);
            } catch (Exception e) {
                Core.getLogger().log(Level.WARNING, "Erro ao tentar verificar se o jogador é premium na API: " + apiUrl, e);
            }
        }
        return ResponseCode.UNKNOWN;
    }

    public CompletableFuture<ResponseCode> isPremiumAsync(String name) {
        return CompletableFuture.supplyAsync(() -> isPremium(name));
    }

    public UUID getUUID(String name) throws Exception {
        validateNickname(name);
        return uuidCache.get(name.toLowerCase()).orElse(null);
    }

    public String getName(UUID uuid) throws Exception {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        return nameCache.get(uuid).orElse(null);
    }

    public Property getTextures(UUID uuid) throws Exception {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        return texturesCache.get(uuid).orElse(null);
    }

    private void validateNickname(String name) throws MojangException {
        if (!Validator.isNickname(name)) {
            throw new MojangException(MojangException.ErrorType.INVALID_NICKNAME);
        }
    }

    public enum ResponseCode {
        DONE, FAIL, UNKNOWN
    }

    @FunctionalInterface
    private interface CallableWithException<T> {
        T call() throws Exception;
    }
}
