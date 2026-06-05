package com.minecraft.proxyhost.velocity.listener;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.util.DisconnectMessages;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Optional;

/**
 * Melhora mensagens ao ser kickado de um backend (servidor fechando, restart, etc.)
 * e redireciona ao lobby quando configurado em {@code routing.on-disconnect-target}.
 */
public final class ServerKickListener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    @Subscribe
    public void onKicked(KickedFromServerEvent event) {
        if (event.getResult() != null) return;

        String serverName = event.getServer().getServerInfo().getName();
        String reason = event.getServerKickReason()
                .map(PLAIN::serialize)
                .map(String::trim)
                .orElse("");

        if (DisconnectMessages.isProtectedKick(reason)) return;

        String redirect = Core.getPlugin().getConfig().getString("routing.on-disconnect-target", "");
        if (redirect != null && !redirect.isBlank()) {
            Optional<RegisteredServer> target = Core.getProxy().getServer(redirect);
            if (target.isPresent() && !target.get().getServerInfo().getName().equalsIgnoreCase(serverName)) {
                event.setResult(KickedFromServerEvent.RedirectPlayer.create(
                        target.get(),
                        DisconnectMessages.redirectNotice(serverName)));
                return;
            }
        }

        Component message;
        if (reason.isBlank() || DisconnectMessages.isGenericReason(reason)) {
            message = DisconnectMessages.serverClosed(serverName);
        } else {
            message = DisconnectMessages.serverKick(serverName, reason);
        }
        event.setResult(KickedFromServerEvent.DisconnectPlayer.create(message));
    }
}
