package com.minecraft.lobbyhost.bukkit.lobby;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.api.clan.ClanTagFormatter;
import com.minecraft.lobbyhost.architect.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Sidebar por jogador — layout espelhado do {@code GameScoreboard},
 * com Cash (rede) no lugar de Saldo (money do game).
 */
public final class LobbyScoreboard {

    public static final String OBJECTIVE_ID = "lobby_sb";

    private LobbyScoreboard() {}

    public static Scoreboard ensure(Player player) {
        Scoreboard board = player.getScoreboard();
        var main = Bukkit.getScoreboardManager().getMainScoreboard();
        if (board == null || board.equals(main)) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(board);
        }
        return board;
    }

    @Deprecated
    public static void animateTitle(Player player) {}

    public static void applyMain(Player player, ServerType server) {
        Scoreboard board = ensure(player);
        String title = LobbySettings.SIDEBAR_TITLE;
        Objective obj = board.getObjective(OBJECTIVE_ID);
        if (obj == null) {
            obj = board.registerNewObjective(OBJECTIVE_ID, Criteria.DUMMY, title);
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            obj.setDisplayName(title);
        }

        Account account = Core.getAccountController().of(player.getUniqueId());
        String rankDisplay = "§7MEMBER";
        if (account != null) {
            rankDisplay = account.getRankType().formatScoreboard();
        }

        String clanDisplay = "§7Nenhum";
        if (account != null && account.hasClan()) {
            var clan = account.getClan();
            if (clan != null) clanDisplay = ClanTagFormatter.formatCompact(clan);
        }

        String sym = LobbySettings.CASH_SYMBOL;
        int cash = account != null ? account.getCash() : 0;
        int online = Bukkit.getOnlinePlayers().size();

        setLine(obj, 15, "§7");
        setLine(obj, 14, "§d❈ §fRank: " + rankDisplay);
        setLine(obj, 13, "§3◆ §fClan: " + clanDisplay);
        setLine(obj, 12, "§b" + sym + " §fCash: §a" + sym + cash);
        setLine(obj, 11, "§a⚡ §fOnline: §a" + online);
        setLine(obj, 10, "§7 ");
        clearLine(obj, 9);
        setLine(obj, 8, "§e" + LobbySettings.SERVER_ADDRESS);
    }

    public static void updateOnlineCount() {
        int online = Bukkit.getOnlinePlayers().size();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Objective obj = player.getScoreboard().getObjective(OBJECTIVE_ID);
            if (obj != null) setLine(obj, 11, "§a⚡ §fOnline: §a" + online);
        }
    }

    public static void refreshCash(Player player) {
        if (player == null || !player.isOnline()) return;
        Account account = Core.getAccountController().of(player.getUniqueId());
        Objective obj = player.getScoreboard().getObjective(OBJECTIVE_ID);
        if (obj == null) {
            applyMain(player, null);
            return;
        }
        String sym = LobbySettings.CASH_SYMBOL;
        int cash = account != null ? account.getCash() : 0;
        setLine(obj, 12, "§b" + sym + " §fCash: §a" + sym + cash);
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
