package com.minecraft.lobbyhost.bukkit.menu.leaderboard;

import com.minecraft.lobbyhost.leaderboard.LeaderboardType;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class LeaderboardHubMenu extends Menu {

    public LeaderboardHubMenu(Player player) {
        super(player, com.minecraft.lobbyhost.util.ModernText.menuTitle("Rankings"), 4, null);
    }

    @Override
    public void build() {
        clear();
        set(4, MenuItem.of(Material.GOLD_BLOCK)
                .name("§6§lLeaderboard Global")
                .lore("§7Dados da tabela §fnetwork_stats",
                        "§7Atualizado pelo Semi Anarquia"));

        int[] slots = {19, 21, 23, 25, 31};
        LeaderboardType[] types = LeaderboardType.values();
        for (int i = 0; i < types.length && i < slots.length; i++) {
            LeaderboardType type = types[i];
            set(slots[i], MenuItem.of(type.icon())
                    .name(type.title())
                    .lore("§7Top 10 global", "", "§eClique para ver")
                    .click(p -> new LeaderboardListMenu(p, type, this).open()));
        }

        closeButton(35);
    }
}
