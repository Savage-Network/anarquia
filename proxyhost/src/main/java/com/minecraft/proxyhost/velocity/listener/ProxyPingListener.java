package com.minecraft.proxyhost.velocity.listener;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.proxy.ProxySettingsManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;

/**
 * MOTD dinamico (inclui modo manutencao e placeholders).
 */
public final class ProxyPingListener {

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        ProxySettingsManager settings = ProxySettingsManager.get();
        if (settings == null) return;

        ServerPing ping = event.getPing();
        ServerPing.Builder builder = ping.asBuilder()
                .description(settings.buildMotdDescription(Core.getProxy()))
                .onlinePlayers(settings.displayOnlinePlayers(Core.getProxy()))
                .maximumPlayers(settings.resolveMaxPlayers(ping));

        String version = settings.buildPingVersionName(Core.getProxy());
        if (version != null) {
            builder.version(new ServerPing.Version(-1, version.replace('&', '§')));
        }

        if (settings.isHidePlayerSample() || settings.isMaintenanceEnabled()) {
            builder.clearSamplePlayers();
        }

        event.setPing(builder.build());
    }
}
