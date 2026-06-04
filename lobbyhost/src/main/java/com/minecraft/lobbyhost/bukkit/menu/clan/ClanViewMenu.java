package com.minecraft.lobbyhost.bukkit.menu.clan;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.clan.Clan;
import com.minecraft.lobbyhost.api.clan.ClanTagFormatter;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class ClanViewMenu extends Menu {

    private final Account account;
    private final Clan clan;

    public ClanViewMenu(Player player, Account account, Clan clan, Menu parent) {
        super(player, com.minecraft.lobbyhost.util.ModernText.menuTitle(clan.getName()), 4, parent);
        this.account = account;
        this.clan = clan;
    }

    @Override
    public void build() {
        clear();
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
                .format(new Date(clan.getCreatedAt()));

        set(13, MenuItem.of(Material.WHITE_BANNER).glow()
                .name("§6§l" + clan.getName())
                .lore(
                        "§7Tag: " + ClanTagFormatter.formatBracketed(clan),
                        "§7Poder: §f" + clan.getPower(),
                        "§7Membros: §f" + clan.getMembers().size(),
                        "§7Fundado: §f" + date
                ));

        set(22, MenuItem.of(Material.PLAYER_HEAD)
                .name("§aVer membros")
                .click(p -> new ClanMembersMenu(p, account, clan, this).open()));

        back(31);
    }
}
