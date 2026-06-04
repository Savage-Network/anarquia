package com.minecraft.hungergames.scoreboard.list;

import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.hungergames.scoreboard.Scoreboard;
import org.bukkit.entity.Player;

public class ScoreboardFinish extends Scoreboard {

    public ScoreboardFinish(com.minecraft.hungergames.Hungergames plugin) {
        super(plugin);
    }

    @Override
    public Sidebar build(Player player) {
        String title = "§6§l" + plugin.getBoardTitle();
        Sidebar sidebar = new Sidebar(player, title);

        sidebar.addRow("status", "Status: §6Finalizando");
        sidebar.addRow("winner", "Vencedor: §a-");
        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        return sidebar;
    }
}
