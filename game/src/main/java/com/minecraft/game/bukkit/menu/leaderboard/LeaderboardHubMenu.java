package com.minecraft.game.bukkit.menu.leaderboard;

import com.minecraft.game.leaderboard.LeaderboardType;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class LeaderboardHubMenu extends Menu {

    public LeaderboardHubMenu(Player player) {
        super(player, "§8§lSavageMC §7| §fRankings", 4, null);
    }

    @Override
    public void build() {
        clear();
        set(4, MenuItem.of(Material.GOLD_BLOCK)
                .name("§6§lRankings SavageMC")
                .lore("§7Top 10 global da rede", "§7Atualizado a cada minuto"));

        int[] slots = {19, 21, 23, 25, 31};
        LeaderboardType[] types = LeaderboardType.values();
        for (int i = 0; i < types.length && i < slots.length; i++) {
            LeaderboardType type = types[i];
            set(slots[i], MenuItem.of(type.icon())
                    .name(type.title())
                    .lore("§7Top 10 jogadores/clãs", "", "§eClique para ver")
                    .click(p -> new LeaderboardListMenu(p, type, this).open()));
        }

        closeButton(35);
    }
}
