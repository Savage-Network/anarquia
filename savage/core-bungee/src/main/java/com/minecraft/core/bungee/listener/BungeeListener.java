package com.minecraft.core.bungee.listener;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bungee.event.list.update.UpdateEvent;
import com.minecraft.core.server.Server;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.list.StringUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BungeeListener implements Listener {

    private final String description = "§6§lVENHA JOGAR!";

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        CompletableFuture.runAsync(() -> {
            Core.getServerData().updatePlayerCounter(ProxyServer.getInstance().getOnlineCount());
            Core.getAccountController().list().forEach(Account::checkInfos);
        });
    }

    @EventHandler
    public void motd(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();

        String motdDescription = description;

        Server server = Core.getServerData().local();

        if (server != null && server.getWhitelist().isEnabled())
            motdDescription = "§c§lEM MANUTENÇÃO!";

        ping.setDescriptionComponent(new TextComponent(StringUtil.StringHelper.makeCenteredMotd("§2§lSAVAGE §f» §ewww.savagemc.net") + "\n" + StringUtil.StringHelper.makeCenteredMotd(motdDescription)));

        event.setResponse(ping);
    }

    @EventHandler
    public void connect(ServerConnectEvent event) {
        ProxiedPlayer proxied = event.getPlayer();
        
        // Verificar whitelist do proxy ao entrar pela primeira vez
        if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            Server localServer = Core.getServerData().local();
            
            if (localServer != null && localServer.getWhitelist().isEnabled()) {
                Account account = Core.getAccountController().of(proxied.getUniqueId());
                
                if (account != null) {
                    String playerName = account.getNickname().toLowerCase();
                    
                    // Verificar se o jogador está na whitelist ou tem permissão de staff
                    if (!localServer.getWhitelist().hasPlayer(playerName) && 
                        !localServer.getWhitelist().hasRank(account.getRank().getType())) {
                        
                        event.setCancelled(true);
                        proxied.disconnect(TextComponent.fromLegacyText(
                            "§c§lWHITELIST ATIVA!\n\n" +
                            "§7O servidor está atualmente em manutenção.\n" +
                            "§7Apenas jogadores autorizados podem entrar.\n\n" +
                            "§7Servidor: §f" + localServer.getNameAndId()
                        ));
                        return;
                    }
                }
            }
        }
        
        // Verificar whitelist do servidor de destino
        if (event.getTarget() != null) {
            Server targetServer = Core.getServerData().findByProxyServer(event.getTarget());
            
            if (targetServer != null && targetServer.getWhitelist().isEnabled()) {
                Account account = Core.getAccountController().of(proxied.getUniqueId());
                
                if (account != null) {
                    String playerName = account.getNickname().toLowerCase();
                    
                    // Verificar se o jogador está na whitelist ou tem permissão de staff
                    if (!targetServer.getWhitelist().hasPlayer(playerName) && 
                        !targetServer.getWhitelist().hasRank(account.getRank().getType())) {
                        
                        event.setCancelled(true);
                        
                        if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
                            proxied.disconnect(TextComponent.fromLegacyText(
                                "§c§lWHITELIST ATIVA!\n\n" +
                                "§7O servidor está atualmente em manutenção.\n" +
                                "§7Apenas jogadores autorizados podem entrar."
                            ));
                        } else {
                            proxied.sendMessage(TextComponent.fromLegacyText(
                                "§cO servidor §e" + targetServer.getName() + "§c está em whitelist!"
                            ));
                        }
                        return;
                    }
                }
            }
        }
        
        if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            Account account = Core.getAccountController().of(proxied.getUniqueId());

            if (account == null) {
                event.setCancelled(true);

                proxied.disconnect(TextComponent.fromLegacyText(Constant.ACCOUNT_LOAD_FAILED_MESSAGE));
                return;
            }

            ServerType category = proxied.getPendingConnection().isOnlineMode() ? ServerType.HUB : ServerType.AUTH;

            Server server = Core.getServerData().of(category);

//            if (server == null || server.getProxyServer() == null) {
//                event.setCancelled(true);
//
//                proxied.disconnect(TextComponent.fromLegacyText(String.format(Constant.SERVER_NOT_FOUND_MESSAGE, category.getName())));
//                return;
//            }

            event.setTarget(server.getProxyServer());
        }
    }
}
