package com.minecraft.lobbyhost.bukkit.lobby;

import com.minecraft.lobbyhost.architect.ServerType;
import org.bukkit.entity.Player;

/**
 * @deprecated Use {@link LobbyScoreboard}.
 */
@Deprecated
public final class SidebarBuilder {

    private SidebarBuilder() {}

    public static void applyMain(Player player, ServerType server) {
        LobbyScoreboard.applyMain(player, server);
    }
}
