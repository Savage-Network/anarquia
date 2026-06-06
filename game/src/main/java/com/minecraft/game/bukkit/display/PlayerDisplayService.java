package com.minecraft.game.bukkit.display;

import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.ClanSync;
import com.minecraft.game.bukkit.manager.PermissionManager;
import com.minecraft.game.bukkit.manager.TagManager;
import com.minecraft.game.bukkit.skin.SkinService;
import org.bukkit.entity.Player;

/** Aplica tab, sidebar, permissões e skin (join + sync Redis). */
public final class PlayerDisplayService {

    private PlayerDisplayService() {}

    public static void applyAll(Account account, Player player) {
        if (account == null || player == null || !player.isOnline()) return;
        ClanSync.refresh(account);
        GameScoreboard.apply(player);
        GameTabList.apply(player);
        PermissionManager.loadPermissions(player);
        SkinService.refresh(account, player);
        TagManager.updateTag(account);
        TagManager.updateAllForViewer(player);
        AboveNameDisplay.remove(player);
    }
}
