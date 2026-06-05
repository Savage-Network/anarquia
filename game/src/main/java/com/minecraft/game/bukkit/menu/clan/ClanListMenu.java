package com.minecraft.game.bukkit.menu.clan;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanTagFormatter;
import com.minecraft.game.backend.data.ClanData;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

/** Lista paginada de todos os clãs. */
public final class ClanListMenu extends Menu {

    private static final int PAGE_SIZE = 21;
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final Account account;
    private final int page;

    public ClanListMenu(Player player, Account account, Menu parent, int page) {
        super(player, "§8§lClãs §7| §fLista §8(p." + (page + 1) + ")", 6, parent);
        this.account = account;
        this.page = page;
    }

    @Override
    public void build() {
        clear();
        ClanData data = Core.getClanData();
        List<Clan> all = data != null
                ? data.listAll().stream()
                .sorted(Comparator.comparing(Clan::getName, String.CASE_INSENSITIVE_ORDER))
                .toList()
                : List.of();

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, all.size());

        set(4, MenuItem.of(Material.BOOK)
                .name("§fClãs do servidor")
                .lore("§7Total: §f" + all.size(), "§7Página: §f" + (page + 1)));

        if (all.isEmpty()) {
            set(22, MenuItem.of(Material.BARRIER)
                    .name("§7Nenhum clã")
                    .lore("§eSeja o primeiro! §f/clan criar"));
        } else {
            int idx = 0;
            for (int i = start; i < end && idx < SLOTS.length; i++) {
                Clan clan = all.get(i);
                set(SLOTS[idx++], MenuItem.of(Material.WHITE_BANNER)
                        .name("§f" + clan.getName())
                        .lore(
                                "§7Tag: " + ClanTagFormatter.formatBracketed(clan),
                                "§7Poder: §f" + clan.getPower(),
                                "§7Membros: §f" + clan.getMembers().size(),
                                "",
                                "§eClique para detalhes"
                        )
                        .click(p -> new ClanViewMenu(p, account, clan, this).open()));
            }
        }

        if (page > 0) {
            set(45, MenuItem.of(Material.ARROW).name("§aAnterior")
                    .click(p -> new ClanListMenu(p, account, parent, page - 1).open()));
        }
        if (end < all.size()) {
            set(53, MenuItem.of(Material.ARROW).name("§aPróxima")
                    .click(p -> new ClanListMenu(p, account, parent, page + 1).open()));
        }

        if (parent != null) back(48);
        closeButton(49);
    }
}
