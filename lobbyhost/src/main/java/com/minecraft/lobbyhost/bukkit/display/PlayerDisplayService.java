package com.minecraft.lobbyhost.bukkit.display;

import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.clan.ClanSync;
import com.minecraft.lobbyhost.architect.Architect;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyScoreboard;
import com.minecraft.lobbyhost.bukkit.lobby.LobbyTabList;
import com.minecraft.lobbyhost.bukkit.manager.PermissionManager;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import com.minecraft.lobbyhost.bukkit.skin.SkinService;
import org.bukkit.entity.Player;

public final class PlayerDisplayService {

    private PlayerDisplayService() {}

    public static void applyAll(Account account, Player player) {
        if (account == null || player == null || !player.isOnline()) return;

        Architect architect = LobbyHostPlugin.getInstance().getArchitectLoader().getArchitect();
        LobbyScoreboard.applyMain(player, architect.getServer());
        LobbyTabList.apply(player);
        PermissionManager.loadPermissions(player);
        SkinService.refresh(account, player);
        ClanSync.refresh(account);
        TagManager.updateTag(account);
        TagManager.updateAllForViewer(player);
        AboveNameDisplay.refresh(player);
        AboveNameDisplay.refreshAllForViewer(player);
    }
}
