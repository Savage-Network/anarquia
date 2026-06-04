package com.minecraft.lobbyhost.bukkit.lobby;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.manager.PermissionManager;
import com.minecraft.lobbyhost.bukkit.skin.SkinService;
import org.bukkit.entity.Player;

/**
 * Setup pos-join estilo {@code DataListener} do Savage (skin + permissoes Bukkit).
 */
public final class LobbyPlayerSetup {

    private LobbyPlayerSetup() {}

    public static void apply(Player player, Account account) {
        if (account == null) return;
        PermissionManager.loadPermissions(player);
        SkinService.refresh(account, player);
    }
}
