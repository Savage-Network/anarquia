package com.minecraft.proxyhost.velocity.listener;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.routing.EventosAccessGuard;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Optional;

/**
 * Impede conexão ao servidor de eventos (menu, /evento, BungeeCord, etc.)
 * enquanto {@code routing.eventos-access.enabled} estiver {@code false}.
 */
public final class EventosConnectListener {

    @Subscribe(order = PostOrder.FIRST)
    public void onServerPreConnect(ServerPreConnectEvent event) {
        RegisteredServer target = event.getOriginalServer();
        if (target == null) return;
        if (!EventosAccessGuard.isBlockedTarget(target.getServerInfo().getName())) return;

        Player player = event.getPlayer();
        player.sendMessage(EventosAccessGuard.blockedMessage());
        event.setResult(ServerPreConnectEvent.ServerResult.denied());
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onChooseInitialServer(PlayerChooseInitialServerEvent event) {
        Optional<RegisteredServer> chosen = event.getInitialServer();
        if (chosen.isEmpty()) return;
        if (!EventosAccessGuard.isBlockedTarget(chosen.get().getServerInfo().getName())) return;

        Player player = event.getPlayer();
        player.sendMessage(EventosAccessGuard.blockedMessage());

        String lobbyName = Core.getPlugin().getConfig().getString("routing.lobby-server", "lobby");
        Optional<RegisteredServer> lobby = Core.getProxy().getServer(lobbyName);
        if (lobby.isPresent()) {
            event.setInitialServer(lobby.get());
        } else {
            player.disconnect(EventosAccessGuard.blockedMessage());
        }
    }
}
