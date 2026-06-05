package com.minecraft.game.bukkit.menu.clan;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanService;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Convida jogadores online sem clã. */
public final class ClanInviteMenu extends Menu {

    private final Account account;
    private final Clan clan;

    public ClanInviteMenu(Player player, Account account, Clan clan, Menu parent) {
        super(player, "§8§lClã §7| §fConvidar", 6, parent);
        this.account = account;
        this.clan = clan;
    }

    @Override
    public void build() {
        clear();

        int slot = 10;
        for (Account online : Core.getAccountController().list()) {
            if (slot > 43) break;
            if (slot % 9 == 0 || slot % 9 == 8) slot++;
            if (online.getId().equals(account.getId())) continue;
            if (online.hasClan()) continue;
            if (!online.getToggle().isAllowClanInvites()) continue;

            int s = slot++;
            set(s, MenuItem.of(Material.PLAYER_HEAD)
                    .skullOwner(online.getName())
                    .name("§f" + online.getNickname())
                    .lore("§7Clique para enviar convite")
                    .click(p -> {
                        clickSound();
                        ClanService.Result r = ClanService.invite(account, online);
                        account.send(r.success() ? "§a" + r.message() : "§c" + r.message());
                        if (r.success()) successSound();
                        reopen();
                    }));
        }

        if (slot == 10) {
            set(22, MenuItem.of(Material.BARRIER)
                    .name("§7Ninguém disponível")
                    .lore("§8Jogadores offline ou já em clã", "§8Use §f/clan convidar <nick>"));
        }

        back(49);
        closeButton(53);
    }
}
