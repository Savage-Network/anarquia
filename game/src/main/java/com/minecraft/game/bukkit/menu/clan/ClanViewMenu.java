package com.minecraft.game.bukkit.menu.clan;

import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanTagFormatter;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class ClanViewMenu extends Menu {

    private final Account account;
    private final Clan clan;

    public ClanViewMenu(Player player, Account account, Clan clan, Menu parent) {
        super(player, "§8§lClã §7| §f" + clan.getName(), 4, parent);
        this.account = account;
        this.clan = clan;
    }

    @Override
    public void build() {
        clear();
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
                .format(new Date(clan.getCreatedAt()));

        set(4, MenuItem.of(Material.WHITE_BANNER).glow()
                .name("§6§l" + clan.getName())
                .lore(
                        "§7Tag: " + ClanTagFormatter.formatBracketed(clan),
                        "§7Poder: §f" + clan.getPower(),
                        "§7Membros: §f" + clan.getMembers().size(),
                        "§7Online: §a" + clan.getOnlineCount(),
                        "§7Fundado: §f" + date,
                        clan.getDescription().isBlank() ? null : "",
                        clan.getDescription().isBlank() ? null : "§7" + clan.getDescription()
                ));

        set(11, MenuItem.of(Material.WRITABLE_BOOK)
                .name("§eMOTD")
                .lore("§f" + clan.getMotd()));

        set(13, MenuItem.of(Material.PLAYER_HEAD)
                .name("§aVer membros")
                .click(p -> new ClanMembersMenu(p, account, clan, this).open()));

        set(15, MenuItem.of(Material.COMPASS)
                .name("§bBase do clã")
                .lore(clan.hasHome() ? "§7Definida em §f" + clan.getStorage().getHomeWorld()
                        : "§7Sem base definida"));

        back(31);
    }
}
