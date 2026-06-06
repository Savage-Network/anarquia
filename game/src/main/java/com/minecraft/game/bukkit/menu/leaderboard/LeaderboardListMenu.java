package com.minecraft.game.bukkit.menu.leaderboard;

import com.minecraft.game.message.MenuTexts;
import com.minecraft.game.backend.model.NetworkStatsEntry;
import com.minecraft.game.leaderboard.LeaderboardCache;
import com.minecraft.game.leaderboard.LeaderboardType;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public final class LeaderboardListMenu extends Menu {

    private final LeaderboardType type;

    public LeaderboardListMenu(Player player, LeaderboardType type, Menu parent) {
        super(player, "§8§lSavageMC §7| " + stripColors(type.title(player)), 6, parent);
        this.type = type;
    }

    @Override
    public void build() {
        clear();
        List<NetworkStatsEntry> top = LeaderboardCache.get(type);
        if (top.isEmpty()) {
            set(22, MenuItem.of(Material.BARRIER)
                    .name(MenuTexts.legacy(player, "menus.common.no-data"))
                    .lore(MenuTexts.legacy(player, "menus.common.no-data-hint")));
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
            case GOLDS -> new String[]{"§7Saldo: §a" + e.getGolds()};
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

    private static String stripColors(String s) {
        return s.replaceAll("§[0-9a-fk-or]", "");
    }
}
