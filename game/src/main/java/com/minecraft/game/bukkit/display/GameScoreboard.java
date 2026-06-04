package com.minecraft.game.bukkit.display;

import com.minecraft.game.Core;
import com.minecraft.game.GameSettings;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.ClanTagFormatter;
import com.minecraft.game.bukkit.semi.CombatTagManager;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Sidebar por jogador + teams do {@link com.minecraft.game.bukkit.manager.TagManager}.
 */
public final class GameScoreboard {

    public static final String OBJECTIVE_ID = "game_sb";

    private GameScoreboard() {}

    public static Scoreboard ensure(Player player) {
        Scoreboard board = player.getScoreboard();
        var main = Bukkit.getScoreboardManager().getMainScoreboard();
        if (board == null || board.equals(main)) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(board);
        }
        return board;
    }

    public static void apply(Player player) {
        Scoreboard board = ensure(player);
        String title = GameSettings.SIDEBAR_TITLE;
        Objective obj = board.getObjective(OBJECTIVE_ID);
        if (obj == null) {
            obj = board.registerNewObjective(OBJECTIVE_ID, Criteria.DUMMY, title);
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        Account account = Core.getAccountController().of(player.getUniqueId());
        String rankDisplay = "§7MEMBER";
        if (account != null) {
            rankDisplay = account.getRankType().getColor() + account.getRankType().name();
        }

        String clanDisplay = "§7Nenhum";
        if (account != null && account.hasClan()) {
            var clan = account.getClan();
            if (clan != null) clanDisplay = ClanTagFormatter.formatBracketed(clan);
        }

        String sym = SemiAnarchyConfig.currencySymbol();
        int golds = account != null ? account.getGolds() : 0;
        int online = Bukkit.getOnlinePlayers().size();

        setLine(obj, 15, "§7");
        setLine(obj, 14, "§d❈ §fRank: " + rankDisplay);
        setLine(obj, 13, "§3◆ §fClan: " + clanDisplay);
        setLine(obj, 12, "§6$ §fSaldo: §a" + sym + golds);
        setLine(obj, 11, "§a⚡ §fOnline: §a" + online);
        setLine(obj, 10, "§7 ");

        if (CombatTagManager.isTagged(player)) {
            setLine(obj, 9, "§c⚔ §fCombate: §c" + CombatTagManager.remainingSeconds(player) + "s");
        } else {
            clearLine(obj, 9);
        }

        setLine(obj, 8, "§e" + GameSettings.SERVER_ADDRESS);
        setLine(obj, 7, "§7Loja: §6" + GameSettings.SERVER_STORE_URL);
    }

    public static void refreshOnlineCount() {
        int online = Bukkit.getOnlinePlayers().size();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Objective obj = player.getScoreboard().getObjective(OBJECTIVE_ID);
            if (obj != null) setLine(obj, 11, "§a⚡ §fOnline: §a" + online);
        }
    }

    public static void refreshCombatTags() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Objective obj = player.getScoreboard().getObjective(OBJECTIVE_ID);
            if (obj == null) continue;
            if (CombatTagManager.isTagged(player)) {
                setLine(obj, 9, "§c⚔ §fCombate: §c" + CombatTagManager.remainingSeconds(player) + "s");
            } else {
                clearLine(obj, 9);
            }
        }
    }

    private static void setLine(Objective obj, int score, String text) {
        var board = obj.getScoreboard();
        if (board == null) return;
        clearLine(obj, score);
        obj.getScore(text).setScore(score);
    }

    private static void clearLine(Objective obj, int score) {
        var board = obj.getScoreboard();
        if (board == null) return;
        for (String entry : board.getEntries()) {
            if (obj.getScore(entry) != null && obj.getScore(entry).getScore() == score) {
                board.resetScores(entry);
            }
        }
    }
}
