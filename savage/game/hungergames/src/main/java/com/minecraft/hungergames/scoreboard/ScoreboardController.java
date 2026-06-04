package com.minecraft.hungergames.scoreboard;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.hungergames.Hungergames;
import com.minecraft.hungergames.game.kit.Kit;
import com.minecraft.hungergames.game.stage.GameStage;
import com.minecraft.hungergames.scoreboard.list.ScoreboardFinish;
import com.minecraft.hungergames.scoreboard.list.ScoreboardPlaying;
import com.minecraft.hungergames.scoreboard.list.ScoreboardWatting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardController {

    private final Hungergames plugin;
    private final Map<UUID, Integer> updateTasks = new HashMap<>();

    ///commit bct
    public ScoreboardController(Hungergames plugin) {
        this.plugin = plugin;
    }

    public void render(Player player, GameStage stage) {
        if (player == null || stage == null) return;

        cancelUpdate(player.getUniqueId());

        Scoreboard builder;
        switch (stage) {
            case WAITING:
            case STARTING:
                builder = new ScoreboardWatting(plugin);
                break;
            case INVINCIBILITY:
            case PLAYING:
            case DEATHMATCH:
                builder = new ScoreboardPlaying(plugin);
                break;
            case ENDING:
            default:
                builder = new ScoreboardFinish(plugin);
                break;
        }

        Sidebar sidebar = builder.build(player);
        if (sidebar != null) startUpdating(player, sidebar);
    }

    public void renderForAll(GameStage stage) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            render(player, stage);
        }
    }

    public void cancelAll() {
        updateTasks.values().forEach(Bukkit.getScheduler()::cancelTask);
        updateTasks.clear();
    }

    public void cancelUpdate(UUID id) {
        Integer taskId = updateTasks.remove(id);
        if (taskId != null) Bukkit.getScheduler().cancelTask(taskId);
    }

    private void startUpdating(Player player, Sidebar sidebar) {
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            GameStage stage = plugin.getCurrentStage();

            long seconds;
            String label;
            switch (stage) {
                case WAITING:
                case STARTING:
                    label = "Iniciando em: ";
                    seconds = plugin.getRemainingSeconds();
                    break;
                case INVINCIBILITY:
                    label = "Invencível por: ";
                    seconds = plugin.getRemainingSeconds();
                    break;
                default:
                    label = "Tempo de jogo: ";
                    seconds = plugin.getElapsedSeconds();
                    break;
            }

            sidebar.updateRow("timer", label + "§a" + formatSeconds(seconds));

            int online = Bukkit.getOnlinePlayers().size();
            int max = Bukkit.getServer().getMaxPlayers();
            sidebar.updateRow("players", "Jogadores: §f" + online + "§7/§f" + max);

            List<Kit> kits = plugin.getKitManager().getPlayerKits(player);
            if (kits.isEmpty()) {
                if (sidebar.hasRow("kit")) sidebar.updateRow("kit", "Kit: §7Nenhum");
            } else if (kits.size() == 1) {
                String name = kits.get(0).getDisplayName();
                if (sidebar.hasRow("kit")) sidebar.updateRow("kit", "Kit: §a" + name);
            } else {
                String k1 = kits.get(0).getDisplayName();
                String k2 = kits.size() >= 2 ? kits.get(1).getDisplayName() : "-";
                if (sidebar.hasRow("kit1")) sidebar.updateRow("kit1", "Kit 1: §a" + k1);
                if (sidebar.hasRow("kit2")) sidebar.updateRow("kit2", "Kit 2: §a" + k2);
            }

            Account account = Core.getAccountController().of(player.getUniqueId());
            if (account != null) {
                sidebar.updateRow("rank", "Rank: §7- " + account.getRank().getColoredName());
            }

        }, 20L, 20L);

        updateTasks.put(player.getUniqueId(), taskId);
    }

    private String formatSeconds(long totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}