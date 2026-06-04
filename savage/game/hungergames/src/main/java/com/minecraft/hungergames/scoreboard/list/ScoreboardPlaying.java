package com.minecraft.hungergames.scoreboard.list;

import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.hungergames.scoreboard.Scoreboard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class ScoreboardPlaying extends Scoreboard {

    public ScoreboardPlaying(com.minecraft.hungergames.Hungergames plugin) {
        super(plugin);
    }

    @Override
    public Sidebar build(Player player) {
        String title = "§6§l" + plugin.getBoardTitle();
        Sidebar sidebar = new Sidebar(player, title);

        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getServer().getMaxPlayers();

        String timerLabel = plugin.getCurrentStage() == com.minecraft.hungergames.game.stage.GameStage.INVINCIBILITY ? "Invencível por: " : "Tempo de jogo: ";
        sidebar.addRow("timer", timerLabel + "§a" + (plugin.getCurrentStage() == com.minecraft.hungergames.game.stage.GameStage.INVINCIBILITY ? formatSeconds(plugin.getRemainingSeconds()) : formatSeconds(plugin.getElapsedSeconds())));
        sidebar.addRow("players", "Jogadores: §f" + online + "§7/§f" + max);
        sidebar.blankRow();
        boolean isDoubleKit = plugin.getBoardTitle() != null && plugin.getBoardTitle().toUpperCase().contains("DOUBLEKIT");
        if (isDoubleKit) {
            sidebar.addRow("kit1", "Kit 1: §7Nenhum");
            sidebar.addRow("kit2", "Kit 2: §7Nenhum");
        } else {
            sidebar.addRow("kit", "Kit: §7Nenhum");
        }
        sidebar.addRow("rank", "Rank: §7- §f...");
        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        return sidebar;
    }

    private String formatSeconds(long totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
