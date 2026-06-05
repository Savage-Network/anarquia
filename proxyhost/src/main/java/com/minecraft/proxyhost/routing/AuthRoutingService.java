package com.minecraft.proxyhost.routing;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.account.context.objects.auth.AuthState;
import com.minecraft.proxyhost.config.ConfigSection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Decide servidor inicial: auth (pirata / nao-original) ou lobby (original Mojang).
 */
public final class AuthRoutingService {

    private AuthRoutingService() {}

    public static RoutingDecision decide(Player player, Account account, ConfigSection cfg) {
        Logger log = Core.getLogger();
        boolean devSkip = cfg.getBoolean("dev.enabled", false);
        String authServer = cfg.getString("routing.auth-server", "auth");
        String lobbyServer = cfg.getString("routing.lobby-server",
                cfg.getString("routing.default-fallback", "lobby"));
        boolean logRouting = cfg.getBoolean("routing.log-decisions", true);

        if (devSkip) {
            if (logRouting) {
                log.info("[routing] {} -> {} (dev.enabled)", player.getUsername(), lobbyServer);
            }
            return new RoutingDecision(lobbyServer, RoutingReason.DEV_BYPASS);
        }

        PlayerOriginDetector.Origin origin = PlayerOriginDetector.detect(player);
        boolean original = origin == PlayerOriginDetector.Origin.ORIGINAL;

        if (original) {
            if (logRouting) {
                log.info("[routing] {} -> {} (conta original Mojang)", player.getUsername(), lobbyServer);
            }
            return new RoutingDecision(lobbyServer, RoutingReason.MOJANG_ORIGINAL);
        }

        boolean trustSaved = cfg.getBoolean("routing.trust-saved-auth", true);
        if (trustSaved && account != null && hasCompletedAuth(account, player.getUniqueId())) {
            if (logRouting) {
                log.info("[routing] {} -> {} (pirata ja autenticado no banco)", player.getUsername(), lobbyServer);
            }
            return new RoutingDecision(lobbyServer, RoutingReason.CRACKED_ALREADY_AUTHED);
        }

        if (logRouting) {
            log.info("[routing] {} -> {} (conta pirata / offline — login no auth)", player.getUsername(), authServer);
        }
        return new RoutingDecision(authServer, RoutingReason.CRACKED_NEEDS_AUTH);
    }

    public static Optional<RegisteredServer> resolveServer(ProxyServer proxy, RoutingDecision decision) {
        Optional<RegisteredServer> primary = proxy.getServer(decision.serverName());
        if (primary.isPresent()) return primary;

        String fallback = Core.getPlugin().getConfig().getString("routing.default-fallback", "lobby");
        Core.getLogger().warn("Servidor '{}' nao encontrado. Tentando '{}'.",
                decision.serverName(), fallback);
        return proxy.getServer(fallback);
    }

    /**
     * Sincroniza {@code premium} e estado de auth para contas originais
     * (nao passam pelo servidor auth /login).
     */
    public static void syncOriginFlag(Player player, Account account) {
        if (account == null) return;
        if (PlayerOriginDetector.isMojangOriginal(player)) {
            applyOriginalBypass(account);
            return;
        }
        if (account.isPremium()) {
            account.setPremium(false);
            account.markDirty();
            Core.getAccountData().saveAsync(account);
        }
    }

    /** Marca conta Mojang como autenticada sem senha local. */
    public static void applyOriginalBypass(Account account) {
        if (account == null) return;
        boolean changed = false;
        if (!account.isPremium()) {
            account.setPremium(true);
            changed = true;
        }
        var auth = account.getContext().getAuth();
        if (auth != null) {
            AuthState state = auth.getState();
            if (state != AuthState.LOGIN && state != AuthState.OK) {
                auth.setState(AuthState.OK);
                auth.setTimestamp(System.currentTimeMillis());
                changed = true;
            }
        }
        if (changed) {
            account.markDirty();
            Core.getAccountData().saveAsync(account);
        }
    }

    private static boolean hasCompletedAuth(Account account, java.util.UUID playerId) {
        if (Core.getPlayerAuthData() != null && Core.getPlayerAuthData().isRegistered(playerId)) {
            return true;
        }
        var auth = account.getContext().getAuth();
        if (auth == null) return false;
        AuthState state = auth.getState();
        return state == AuthState.LOGIN || state == AuthState.OK;
    }

    public record RoutingDecision(String serverName, RoutingReason reason) {}

    public enum RoutingReason {
        DEV_BYPASS,
        MOJANG_ORIGINAL,
        CRACKED_ALREADY_AUTHED,
        CRACKED_NEEDS_AUTH
    }
}
