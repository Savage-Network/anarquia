package com.minecraft.proxyhost.velocity.listener;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.proxy.MaintenanceActionBar;
import com.minecraft.proxyhost.util.DisconnectMessages;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;

/**
 * Cache de conta no proxy. Roteamento auth/lobby: {@link com.minecraft.proxyhost.routing.InitialServerListener}.
 */
public final class LoginListener {

    @Subscribe(order = PostOrder.LATE)
    public void onLogin(LoginEvent event) {
        if (!event.getResult().isAllowed()) return;
        Player player = event.getPlayer();
        Core.getAccountController().load(player.getUniqueId(), player.getUsername())
                .thenAccept(account -> {
                    if (Core.getNetworkPresenceData() != null) {
                        String server = player.getCurrentServer()
                                .map(s -> s.getServerInfo().getName())
                                .orElse("proxy");
                        Core.getNetworkPresenceData().upsert(player.getUniqueId(), player.getUsername(), server);
                    }
                    if (Core.getLogger().isDebugEnabled()) {
                        Core.getLogger().debug("Account cache: {} rank={} permNodes={} premium={}",
                                account.getName(), account.getRankType(),
                                account.collectPermissionNodes().size(), account.isPremium());
                    }
                })
                .exceptionally(t -> {
                    Core.getLogger().error("Falha ao carregar account de "
                            + player.getUsername() + ": " + t.getMessage(), t);
                    player.disconnect(DisconnectMessages.accountLoadFailed());
                    return null;
                });
    }

    @Subscribe
    public void onServerSwitch(ServerPostConnectEvent event) {
        Player p = event.getPlayer();
        MaintenanceActionBar.sync(p);
        Account a = Core.getAccountController().of(p.getUniqueId());
        if (a == null) return;

        if (Core.getNetworkPresenceData() != null) {
            String server = p.getCurrentServer()
                    .map(s -> s.getServerInfo().getName())
                    .orElse("proxy");
            Core.getNetworkPresenceData().upsert(p.getUniqueId(), p.getUsername(), server);
        }

        if (a.isStaffer()) {
            Core.getLogger().info("[staff] {} -> {}",
                    a.getName(),
                    p.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("?"));
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        if (Core.getNetworkPresenceData() != null) {
            Core.getNetworkPresenceData().remove(player.getUniqueId());
        }
        Core.getAccountController().unload(player.getUniqueId());
    }
}
