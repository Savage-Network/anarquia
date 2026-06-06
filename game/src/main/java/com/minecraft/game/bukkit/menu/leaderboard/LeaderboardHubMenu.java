package com.minecraft.game.bukkit.menu.leaderboard;

import com.minecraft.game.leaderboard.LeaderboardType;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.message.MenuTexts;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class LeaderboardHubMenu extends Menu {

    public LeaderboardHubMenu(Player player) {
        super(player, MenuTexts.title(player, "menus.leaderboard.hub-title"), 4, null);
    }

    @Override
    public void build() {
        clear();
        set(4, MenuItem.of(Material.GOLD_BLOCK)
                .name(MenuTexts.legacy(player, "menus.leaderboard.hub-header"))
                .lore(
                        MenuTexts.legacy(player, "menus.leaderboard.hub-desc"),
                        MenuTexts.legacy(player, "menus.leaderboard.hub-update")));

        int[] slots = {19, 21, 23, 25, 31};
        LeaderboardType[] types = LeaderboardType.values();
        for (int i = 0; i < types.length && i < slots.length; i++) {
            LeaderboardType type = types[i];
            set(slots[i], MenuItem.of(type.icon())
                    .name(type.title(player))
                    .lore(
                            MenuTexts.legacy(player, "menus.leaderboard.list-desc"),
                            "",
                            MenuTexts.legacy(player, "menus.common.click-open"))
                    .click(p -> new LeaderboardListMenu(p, type, this).open()));
        }

        closeButton(35);
    }
}
