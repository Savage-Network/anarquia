package com.minecraft.proxyhost.velocity.listener;

import com.minecraft.proxyhost.Constant;
import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.proxy.ProxySettingsManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Carrega/descarrega a {@link Account} no proxy. Decide o servidor inicial:
 *
 * <ul>
 *   <li>Se o jogador ainda nao se autenticou (sem rank > MEMBER salvo) -> {@code auth-server}</li>
 *   <li>Caso contrario -> {@code default-fallback} (geralmente o lobby)</li>
 * </ul>
 */
public final class LoginListener {

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
                    Component.text(Constant.ACCOUNT_LOAD_FAILED_MESSAGE).color(NamedTextColor.RED)));
        }
    }

    @Subscribe(order = PostOrder.LATE)
    public void onLogin(LoginEvent event) {
        if (!event.getResult().isAllowed()) return;
        Player player = event.getPlayer();
        Core.getAccountController().load(player.getUniqueId(), player.getUsername())
                .thenAccept(account -> {
                    account.setIpAddress(player.getRemoteAddress().getAddress().getHostAddress());
                    account.updateLastLogin();
                    if (Core.getLogger().isDebugEnabled()) {
                        Core.getLogger().debug("Account carregada: {} (rank={})",
                                account.getName(), account.getRankType());
                    }
                })
                .exceptionally(t -> {
                    Core.getLogger().error("Falha ao carregar account de "
                            + player.getUsername() + ": " + t.getMessage(), t);
                    player.disconnect(Component.text(Constant.ACCOUNT_LOAD_FAILED_MESSAGE)
                            .color(NamedTextColor.RED));
                    return null;
                });
    }

    @Subscribe(order = PostOrder.LATE)
    public void onChooseInitialServer(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();
        ProxyServer proxy = Core.getProxy();
        var cfg = Core.getPlugin().getConfig();

        String authName = cfg.getString("routing.auth-server", "auth");
        String fallbackName = cfg.getString("routing.default-fallback", "lobby");
        boolean devMode = cfg.getBoolean("dev.enabled", false);

        Account account = Core.getAccountController().of(player.getUniqueId());
        boolean needAuth = !devMode && (account == null || !account.isPremium());

        String target = needAuth ? authName : fallbackName;
        Optional<RegisteredServer> server = proxy.getServer(target);

        if (server.isEmpty()) {
            Core.getLogger().warn("Servidor inicial '{}' nao encontrado. Tentando fallback '{}'.",
                    target, fallbackName);
            server = proxy.getServer(fallbackName);
        }

        server.ifPresent(event::setInitialServer);
    }

    @Subscribe
    public void onServerSwitch(ServerPostConnectEvent event) {
        Player p = event.getPlayer();
        Account a = Core.getAccountController().of(p.getUniqueId());
        if (a == null) return;
        if (a.isStaffer()) {
            Core.getLogger().info("[staff] {} -> {}",
                    a.getName(),
                    p.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("?"));
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        Core.getAccountController().unload(player.getUniqueId());
    }
}
