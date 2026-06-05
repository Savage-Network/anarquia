package com.minecraft.eventos.bukkit.skin;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minecraft.eventos.Core;
import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.account.Account;
import com.minecraft.eventos.account.context.objects.skin.SkinMetadata;
import com.minecraft.eventos.util.AccountSync;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Carrega, persiste e aplica skins customizadas no servidor de eventos.
 * Espelho do SkinService do lobbyhost.
 */
public final class SkinService {

    private static final long STALE_MS = 86_400_000L; // 24h
    private static final String USER_AGENT = "SavageAnarquia-Eventos/1.0";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(8))
            .build();

    private SkinService() {}

    /** Aplica skin armazenada; se não tiver, captura do perfil atual. */
    public static void refresh(Account account, Player player) {
        if (account == null || player == null || !player.isOnline()) return;
        SkinMetadata stored = account.getSkin();
        if (stored != null && stored.isValid()) {
            apply(player, stored);
            return;
        }
        SkinMetadata captured = captureFrom(player);
        if (captured.isValid()) {
            account.setSkin(captured);
            apply(player, captured);
        }
    }

    /** Aplica a skin armazenada na conta. */
    public static void apply(Account account, Player player) {
        if (account == null || player == null || !player.isOnline()) return;
        SkinMetadata skin = account.getSkin();
        if (skin == null || !skin.isValid()) {
            refresh(account, player);
            return;
        }
        apply(player, skin);
    }

    /** Aplica textura diretamente no PlayerProfile. */
    public static void apply(Player player, SkinMetadata skin) {
        if (player == null || skin == null || !skin.isValid()) return;
        try {
            PlayerProfile profile = player.getPlayerProfile();
            profile.removeProperty("textures");
            profile.setProperty(new ProfileProperty("textures", skin.getValue(),
                    skin.getSignature() != null ? skin.getSignature() : ""));
            player.setPlayerProfile(profile);
            refreshViewers(player);
        } catch (Throwable t) {
            Core.getLogger().warning("[SkinService] Falha ao aplicar skin: " + t.getMessage());
        }
    }

    /** Captura textura atual do perfil Mojang do jogador online. */
    public static SkinMetadata captureFrom(Player player) {
        try {
            PlayerProfile profile = player.getPlayerProfile();
            ProfileProperty textures = profile.getProperties().stream()
                    .filter(p -> "textures".equalsIgnoreCase(p.getName()))
                    .findFirst().orElse(null);
            if (textures == null || textures.getValue() == null || textures.getValue().isEmpty()) {
                return new SkinMetadata();
            }
            return SkinMetadata.fromTextures(textures.getValue(), textures.getSignature(), player.getName());
        } catch (Throwable t) {
            return new SkinMetadata();
        }
    }

    /** Busca skin de um jogador pelo nome via API Mojang (async). */
    public static CompletableFuture<Optional<SkinMetadata>> fetchMojang(String playerName) {
        String name = normalizeName(playerName);
        if (name == null) return CompletableFuture.completedFuture(Optional.empty());
        return CompletableFuture.supplyAsync(() -> {
            try {
                Optional<UUID> uuid = resolveUuid(name);
                if (uuid.isEmpty()) return Optional.empty();
                return fetchProfile(uuid.get(), name);
            } catch (Exception e) {
                Core.getLogger().warning("[SkinService] fetchMojang " + name + ": " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    /** Aplica skin de outro jogador Mojang na conta e salva. */
    public static void applyMojangSkin(Account account, Player player, String sourceName,
                                       Runnable onSuccess, Runnable onFail) {
        fetchMojang(sourceName).thenAccept(opt -> Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
            if (!player.isOnline() || account == null) return;
            if (opt.isEmpty()) {
                if (onFail != null) onFail.run();
                return;
            }
            account.setSkin(opt.get());
            apply(player, opt.get());
            AccountSync.saveAndBroadcast(account);
            if (onSuccess != null) onSuccess.run();
        }));
    }

    public static boolean isStale(SkinMetadata skin) {
        if (skin == null || !skin.isValid()) return true;
        return skin.getUpdatedAt() <= 0 || System.currentTimeMillis() - skin.getUpdatedAt() > STALE_MS;
    }

    /** Se a skin estiver velha (>24h), rebusca da Mojang. */
    public static void refreshFromMojangIfStale(Account account, Player player) {
        if (account == null || player == null || !player.isOnline()) return;
        if (!isStale(account.getSkin())) return;
        String source = account.isFake() ? account.getNickname() : account.getName();
        applyMojangSkin(account, player, source, null, null);
    }

    // ---- helpers privados ----

    private static String normalizeName(String raw) {
        if (raw == null) return null;
        String name = raw.trim();
        return name.matches("^[a-zA-Z0-9_]{3,16}$") ? name : null;
    }

    private static Optional<UUID> resolveUuid(String name) throws Exception {
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8);
        Optional<UUID> r = requestUuid("https://api.mojang.com/users/profiles/minecraft/" + encoded);
        if (r.isPresent()) return r;
        return requestUuid("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + encoded);
    }

    private static Optional<UUID> requestUuid(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(8))
                .header("User-Agent", USER_AGENT).GET().build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 204 || res.statusCode() == 404) return Optional.empty();
        if (res.statusCode() != 200) return Optional.empty();
        return parseUuidBody(res.body());
    }

    private static Optional<UUID> parseUuidBody(String body) {
        if (body == null || body.isBlank()) return Optional.empty();
        JsonElement root = JsonParser.parseString(body);
        JsonObject obj;
        if (root.isJsonObject()) {
            obj = root.getAsJsonObject();
        } else if (root.isJsonArray()) {
            JsonArray arr = root.getAsJsonArray();
            if (arr.isEmpty()) return Optional.empty();
            obj = arr.get(0).getAsJsonObject();
        } else return Optional.empty();
        if (!obj.has("id")) return Optional.empty();
        return Optional.of(uuidFromMojang(obj.get("id").getAsString()));
    }

    private static Optional<SkinMetadata> fetchProfile(UUID uuid, String displayName) throws Exception {
        String undashed = uuid.toString().replace("-", "");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/"
                        + undashed + "?unsigned=false"))
                .timeout(java.time.Duration.ofSeconds(8))
                .header("User-Agent", USER_AGENT).GET().build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200 || res.body() == null || res.body().isBlank()) return Optional.empty();
        JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
        if (!root.has("properties")) return Optional.empty();
        for (JsonElement el : root.getAsJsonArray("properties")) {
            JsonObject prop = el.getAsJsonObject();
            if (!"textures".equalsIgnoreCase(prop.get("name").getAsString())) continue;
            String value = prop.get("value").getAsString();
            String signature = prop.has("signature") ? prop.get("signature").getAsString() : "";
            return Optional.of(SkinMetadata.fromTextures(value, signature, displayName));
        }
        return Optional.empty();
    }

    private static UUID uuidFromMojang(String id) {
        String dashed = id.contains("-") ? id : id.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5");
        return UUID.fromString(dashed);
    }

    private static void refreshViewers(Player player) {
        EventosPlugin plugin = Core.getJavaPlugin();
        if (plugin == null) return;
        for (Player viewer : player.getWorld().getPlayers()) {
            if (viewer.equals(player)) continue;
            viewer.hidePlayer(plugin, player);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            for (Player viewer : player.getWorld().getPlayers()) {
                if (viewer.equals(player)) continue;
                viewer.showPlayer(plugin, player);
            }
        }, 2L);
    }
}
