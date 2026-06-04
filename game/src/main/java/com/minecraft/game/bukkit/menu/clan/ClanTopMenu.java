package com.minecraft.game.bukkit.menu.clan;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanTagFormatter;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public final class ClanTopMenu extends Menu {

    private final Account account;

    public ClanTopMenu(Player player, Account account, Menu parent) {
        super(player, "§8§lClãs §7| §fRanking", 5, parent);
        this.account = account;
    }

    @Override
    public void build() {
        clear();

        List<Clan> top = Core.getClanData().listTop(7);
        int[] slots = {20, 21, 22, 23, 24, 30, 31};
        Material[] icons = {
                Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.COPPER_BLOCK,
                Material.EMERALD, Material.LAPIS_BLOCK, Material.AMETHYST_BLOCK, Material.QUARTZ_BLOCK
        };

        if (top.isEmpty()) {
            set(22, MenuItem.of(Material.BARRIER)
                    .name("§7Nenhum clã ainda")
                    .lore("§eSeja o primeiro! §f/clan criar"));
        } else {
            for (int i = 0; i < top.size() && i < slots.length; i++) {
                Clan clan = top.get(i);
                set(slots[i], MenuItem.of(icons[i])
                        .name("§6#" + (i + 1) + " §f" + clan.getName())
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

        back(40);
        closeButton(44);
    }
}
