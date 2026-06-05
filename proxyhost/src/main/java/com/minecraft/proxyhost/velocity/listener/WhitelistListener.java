package com.minecraft.proxyhost.velocity.listener;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.proxy.ProxySettingsManager;
import com.minecraft.proxyhost.util.DisconnectMessages;
import com.minecraft.proxyhost.util.WhitelistMessages;
import com.minecraft.proxyhost.whitelist.WhitelistManager;
import com.minecraft.proxyhost.whitelist.WhitelistTarget;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Bloqueia conexões antes de tentar o backend e envia mensagem formatada no chat
 * (ou tela de desconexão no login inicial).
 */
public final class WhitelistListener {

    @Subscribe(order = PostOrder.FIRST)
    public void onLoginProxyWhitelist(LoginEvent event) {
        WhitelistManager wl = WhitelistManager.get();
        if (wl == null || !wl.isEnabled(WhitelistTarget.PROXY)) return;
        if (!event.getResult().isAllowed()) return;

        Player player = event.getPlayer();
        try {
            Account account = Core.getAccountController()
                    .load(player.getUniqueId(), player.getUsername())
                    .get(10, TimeUnit.SECONDS);
            if (!wl.canAccessProxy(account, player)) {
                event.setResult(LoginEvent.ComponentResult.denied(
                        WhitelistMessages.loginDenied(WhitelistTarget.PROXY, wl.rule(WhitelistTarget.PROXY))));
            }
        } catch (Exception e) {
            Core.getLogger().warn("Whitelist proxy: falha ao carregar conta de {}: {}",
                    player.getUsername(), e.getMessage());
            event.setResult(LoginEvent.ComponentResult.denied(DisconnectMessages.accountLoadFailed()));
        }
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onChooseInitialServer(PlayerChooseInitialServerEvent event) {
        WhitelistManager wl = WhitelistManager.get();
        if (wl == null) return;

        Player player = event.getPlayer();
        Optional<RegisteredServer> chosen = event.getInitialServer();
        if (chosen.isEmpty()) return;

        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) {
            try {
                account = Core.getAccountController()
                        .load(player.getUniqueId(), player.getUsername())
                        .get(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
        }

        RegisteredServer server = chosen.get();
        WhitelistTarget target = WhitelistTarget.fromVelocityServer(server.getServerInfo().getName());
        if (target == null || wl.canAccess(account, player, target)) return;

        Optional<RegisteredServer> fallback = resolveFallback(player, account, wl, target);
        if (fallback.isPresent()) {
            event.setInitialServer(fallback.get());
            player.sendMessage(WhitelistMessages.chatDenied(target, wl.rule(target)));
            return;
        }

        player.disconnect(WhitelistMessages.loginDenied(target, wl.rule(target)));
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onServerPreConnect(ServerPreConnectEvent event) {
        WhitelistManager wl = WhitelistManager.get();
        if (wl == null) return;

        Player player = event.getPlayer();
        RegisteredServer target = event.getOriginalServer();
        if (target == null) return;

        WhitelistTarget wlTarget = WhitelistTarget.fromVelocityServer(target.getServerInfo().getName());
        if (wlTarget == null) return;

        Account account = Core.getAccountController().of(player.getUniqueId());
        if (account == null) {
            try {
                account = Core.getAccountController()
                        .load(player.getUniqueId(), player.getUsername())
                        .get(3, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
        }

        if (wl.canAccess(account, player, wlTarget)) return;

        player.sendMessage(WhitelistMessages.chatDenied(wlTarget, wl.rule(wlTarget)));
        event.setResult(ServerPreConnectEvent.ServerResult.denied());
    }

    private Optional<RegisteredServer> resolveFallback(Player player, Account account,
                                                         WhitelistManager wl, WhitelistTarget blocked) {
        if (blocked == WhitelistTarget.LOBBY) return Optional.empty();

        String lobbyName = Core.getPlugin().getConfig().getString("routing.lobby-server", "lobby");
        Optional<RegisteredServer> lobby = Core.getProxy().getServer(lobbyName);
        if (lobby.isEmpty()) return Optional.empty();

        WhitelistTarget lobbyTarget = WhitelistTarget.LOBBY;
        if (wl.canAccess(account, player, lobbyTarget)) {
            return lobby;
        }
        return Optional.empty();
    }
}
