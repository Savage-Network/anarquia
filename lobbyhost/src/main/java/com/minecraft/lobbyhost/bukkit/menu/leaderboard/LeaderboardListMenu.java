package com.minecraft.lobbyhost.bukkit.menu.leaderboard;

import com.minecraft.lobbyhost.backend.model.NetworkStatsEntry;
import com.minecraft.lobbyhost.leaderboard.LeaderboardCache;
import com.minecraft.lobbyhost.leaderboard.LeaderboardType;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public final class LeaderboardListMenu extends Menu {

    private final LeaderboardType type;

    public LeaderboardListMenu(Player player, LeaderboardType type, Menu parent) {
        super(player, com.minecraft.lobbyhost.util.ModernText.menuTitle(type.title()), 6, parent);
        this.type = type;
    }

    @Override
    public void build() {
        clear();
        List<NetworkStatsEntry> top = LeaderboardCache.get(type);
        if (top.isEmpty()) {
            set(22, MenuItem.of(Material.BARRIER)
                    .name("§7Sem dados ainda")
                    .lore("§7Jogue no Semi Anarquia para aparecer aqui."));
        } else {
            int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
            for (int i = 0; i < top.size() && i < slots.length; i++) {
                NetworkStatsEntry e = top.get(i);
                set(slots[i], MenuItem.of(rankMaterial(i))
                        .name("§6#" + (i + 1) + " §f" + e.getDisplayName())
                        .lore(formatValue(e)));
            }
        }
        back(45);
        closeButton(53);
    }

    private String[] formatValue(NetworkStatsEntry e) {
        return switch (type) {
            case KILLS -> new String[]{
                    "§7Kills: §a" + e.getKills(),
                    "§7Melhor streak: §f" + e.getBestKillStreak()
            };
            case DEATHS -> new String[]{"§7Mortes: §c" + e.getDeaths()};
            case GOLDS -> new String[]{"§7Money (game): §a" + e.getGolds()};
            case KDR -> new String[]{
                    String.format("§7K/D: §e%.2f", e.kdr()),
                    "§7" + e.getKills() + "K / " + e.getDeaths() + "D"
            };
            case CLANS -> new String[]{
                    "§7Poder: §f" + e.getClanPower(),
                    "§7Membros: §f" + e.getClanMembers()
            };
        };
    }

    private static Material rankMaterial(int index) {
        return switch (index) {
            case 0 -> Material.GOLD_BLOCK;
            case 1 -> Material.IRON_BLOCK;
            case 2 -> Material.COPPER_BLOCK;
            default -> Material.QUARTZ_BLOCK;
        };
    }
}
