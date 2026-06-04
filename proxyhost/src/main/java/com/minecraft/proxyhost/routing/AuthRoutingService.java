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
        if (trustSaved && account != null && hasCompletedAuth(account)) {
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

    /** Persiste flag de conta original na conta (campo {@code premium} = Mojang original). */
    public static void syncOriginFlag(Player player, Account account) {
        if (account == null) return;
        boolean original = PlayerOriginDetector.isMojangOriginal(player);
        if (account.isPremium() != original) {
            account.setPremium(original);
            Core.getAccountData().saveAsync(account);
        }
    }

    private static boolean hasCompletedAuth(Account account) {
        var auth = account.getContext().getAuth();
        if (auth == null) return false;
        AuthState state = auth.getState();
        String password = auth.getPassword();
        boolean hasPassword = password != null && !password.isEmpty();
        return hasPassword && (state == AuthState.OK || state == AuthState.LOGIN);
    }

    public record RoutingDecision(String serverName, RoutingReason reason) {}

    public enum RoutingReason {
        DEV_BYPASS,
        MOJANG_ORIGINAL,
        CRACKED_ALREADY_AUTHED,
        CRACKED_NEEDS_AUTH
    }
}
