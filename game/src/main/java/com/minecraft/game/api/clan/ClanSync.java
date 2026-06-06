package com.minecraft.game.api.clan;

import com.minecraft.game.Constant;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.manager.TagManager;

/**
 * Sincroniza metadados de cla na conta e atualiza tab/nametag.
 */
public final class ClanSync {

    private ClanSync() {}

    public static void refresh(Account account) {
        if (account == null) return;
        var meta = account.getContext().getClan();
        if (!account.hasClan()) {
            meta.setId(Constant.DEFAULT_ID);
            meta.setTag("");
            meta.setTagColorId("padrao");
        } else {
            Clan clan = account.getClan();
            if (clan != null) {
                clan.normalizeStyle();
                meta.setId(clan.getId());
                meta.setTag(clan.getTagWithoutColors());
                meta.setTagColorId(clan.getStyle().getId());
            }
        }
        meta.setLastUpdate(System.currentTimeMillis());
        account.markDirty();
        if (account.isOnline()) {
            TagManager.updateTag(account);
            if (account.player() != null) {
                com.minecraft.game.bukkit.display.GameScoreboard.apply(account.player());
            }
        }
    }

    public static void refreshMembers(Clan clan) {
        if (clan == null) return;
        for (Account online : clan.getOnlineMembers()) {
            refresh(online);
        }
    }
}
