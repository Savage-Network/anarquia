package com.minecraft.core.server;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.server.flag.ServerFlag;
import com.minecraft.core.server.payload.ServerPayload;
import com.minecraft.core.server.payload.whitelist.ServerWhitelist;
import com.minecraft.core.server.type.ServerType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class Server {

    private final int id, port;
    private final ServerType type;

    private final ServerPayload payload;

    @Override
    public boolean equals(Object serverObj) {
        if (this == serverObj) return true;
        if (serverObj == null || getClass() != serverObj.getClass()) return false;

        Server server = (Server) serverObj;

        return server.getId() == id && server.getPort() == port && server.getType() == type;
    }

    public void refresh() {
        Core.getServerData().refresh(this);
    }

    public boolean isDead() {
        return payload.getHeartBeat() + 5000L < System.currentTimeMillis();
    }

    public ArcadeCategory getArcade() {
        return ArcadeCategory.of(type.getName());
    }

    public boolean isArcade() {
        if (type.equals(ServerType.HUB)) return false;

        return type.isArcade();
    }

    public void setWhitelist(boolean enabled, List<RankType> ranks, List<String> players) {
        ServerWhitelist whitelist = getWhitelist();

        whitelist.setEnabled(enabled);

        whitelist.getRanks().addAll(ranks);
        whitelist.getPlayers().addAll(players);

        refresh();
    }

    public ServerWhitelist getWhitelist() {
        return payload.getWhitelist();
    }

    public String getNameAndId() {
        return type.name().toLowerCase() + "-" + id;
    }

    public String getIdentifier() {
        return type.name().toLowerCase() + ":id-" + id + ":port-" + port; // hub:id-1:port-25565
    }

    public String getName() {
        return type.getName();
    }

    public ServerInfo getProxyServer() {
        String serverName = type.getId().toLowerCase() + (type.hasFlag(ServerFlag.MULTI_SERVER) ? "-" + id : ""); // hub or hub-1
        
        // Tentar buscar com o nome exato primeiro
        ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(serverName);
        
        if (serverInfo != null) {
            return serverInfo;
        }
        
        // Se não encontrou, tentar com maiúsculas
        serverInfo = ProxyServer.getInstance().getServerInfo(serverName.toUpperCase());
        
        if (serverInfo != null) {
            return serverInfo;
        }
        
        // Se ainda não encontrou, tentar buscar case-insensitive
        for (String name : ProxyServer.getInstance().getServers().keySet()) {
            if (name.equalsIgnoreCase(serverName)) {
                return ProxyServer.getInstance().getServerInfo(name);
            }
        }
        
        return null;
    }
}
