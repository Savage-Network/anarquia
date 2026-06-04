package com.minecraft.proxyhost.routing;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.proxy.ProxySettingsManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.concurrent.TimeUnit;

/**
 * Roteamento inicial: original Mojang → lobby; pirata/offline → auth.
 */
public final class InitialServerListener {

    @Subscribe(order = PostOrder.FIRST)
    public void onLoginMaintenance(LoginEvent event) {
        ProxySettingsManager settings = ProxySettingsManager.get();
        if (settings == null || !settings.isMaintenanceEnabled()) return;

        Player player = event.getPlayer();
        try {
            Account account = Core.getAccountController()
                    .load(player.getUniqueId(), player.getUsername())
                    .get(10, TimeUnit.SECONDS);
            if (!settings.canJoin(account, player)) {
                event.setResult(LoginEvent.ComponentResult.denied(settings.maintenanceKickComponent()));
            }
        } catch (Exception e) {
            Core.getLogger().warn("Manutencao: falha ao carregar conta de {}: {}",
                    player.getUsername(), e.getMessage());
            event.setResult(LoginEvent.ComponentResult.denied(
                    Component.text(com.minecraft.proxyhost.Constant.ACCOUNT_LOAD_FAILED_MESSAGE)
                            .color(NamedTextColor.RED)));
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onChooseInitialServer(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();
        var cfg = Core.getPlugin().getConfig();

        try {
            Account account = Core.getAccountController()
                    .load(player.getUniqueId(), player.getUsername())
                    .get(12, TimeUnit.SECONDS);

            account.setIpAddress(player.getRemoteAddress().getAddress().getHostAddress());
            account.updateLastLogin();
            AuthRoutingService.syncOriginFlag(player, account);

            var decision = AuthRoutingService.decide(player, account, cfg);
            AuthRoutingService.resolveServer(Core.getProxy(), decision)
                    .ifPresent(event::setInitialServer);

        } catch (Exception e) {
            Core.getLogger().error("Falha ao rotear {}: {}", player.getUsername(), e.getMessage(), e);
            String lobby = cfg.getString("routing.default-fallback", "lobby");
            Core.getProxy().getServer(lobby).ifPresent(event::setInitialServer);
        }
    }
}
