package com.minecraft.game.bukkit.menu.stats;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.stats.GameStats;
import com.minecraft.game.account.context.objects.stats.PlayerProgress;
import com.minecraft.game.progress.Achievement;
import com.minecraft.game.progress.AchievementService;
import com.minecraft.game.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class StatsMenu {

    private StatsMenu() {}

    public static void open(Player player, Account account) {
        if (account == null) return;
        GameStats stats = account.getGameStats();
        PlayerProgress progress = account.getPlayerProgress();
        AchievementService.checkStatsAchievements(account);

        Inventory inv = Bukkit.createInventory(null, 54,
                ModernText.parse("<!italic><gradient:#38BDF8:#818CF8><bold>Estatísticas</bold></gradient>"));

        inv.setItem(10, icon(Material.IRON_SWORD, "§fCombate",
                List.of("§7Kills: §a" + stats.getKills(),
                        "§7Mortes: §c" + stats.getDeaths(),
                        "§7K/D: §e" + String.format("%.2f", account.getKdr()),
                        "§7Streak atual: §c" + stats.getKillStreak(),
                        "§7Melhor streak: §6" + stats.getBestKillStreak(),
                        "§7Bounty: §6" + stats.getBounty())));

        inv.setItem(12, icon(Material.CLOCK, "§fLogin",
                List.of("§7Streak: §e" + progress.getLoginStreak() + " dias",
                        "§7Total logins: §f" + progress.getTotalLoginDays())));

        inv.setItem(14, icon(Material.GOLD_INGOT, "§fEconomia",
                List.of("§7Saldo: §a" + account.getGolds(),
                        account.hasClan() && account.getClan() != null
                                ? "§7Banco clã: §a" + account.getClan().getStorage().getBankBalance()
                                : "§7Sem clã")));

        inv.setItem(16, icon(Material.NETHER_STAR, "§fConquistas",
                List.of("§7Desbloqueadas: §a" + AchievementService.unlockedCount(account)
                        + "§7/§f" + AchievementService.totalCount(),
                        "§8Clique nos troféus abaixo")));

        int slot = 28;
        for (Achievement achievement : Achievement.values()) {
            if (slot >= 44) break;
            boolean done = progress.getAchievements() != null
                    && progress.getAchievements().contains(achievement.getId());
            inv.setItem(slot++, icon(done ? Material.GOLD_BLOCK : Material.GRAY_DYE,
                    (done ? "§a" : "§7") + achievement.getTitle(),
                    List.of("§7" + achievement.getDescription(),
                            done ? "§a✔ Desbloqueada" : "§8Bloqueada",
                            "§8Recompensa: §a+" + achievement.getGoldReward())));
        }

        inv.setItem(49, icon(Material.BARRIER, "§cFechar", List.of()));
        player.openInventory(inv);
    }

    private static ItemStack icon(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(ModernText.noItalic(ModernText.parse(name)));
        List<net.kyori.adventure.text.Component> lines = new ArrayList<>();
        for (String line : lore) lines.add(ModernText.noItalic(ModernText.parse(line)));
        meta.lore(lines);
        stack.setItemMeta(meta);
        return stack;
    }
}
