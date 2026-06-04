package com.minecraft.lobbyhost.bukkit.lobby;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.clan.ClanTagFormatter;
import com.minecraft.lobbyhost.architect.ServerType;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Scoreboard unico por jogador: sidebar + teams do {@code TagManager}.
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

    public static void applyMain(Player player, ServerType server) {
        Scoreboard board = ensure(player);
        Objective obj = board.getObjective(OBJECTIVE_ID);
        if (obj == null) {
            obj = board.registerNewObjective(
                    OBJECTIVE_ID,
                    Criteria.DUMMY,
                    ModernText.parse(LobbySettings.SIDEBAR_TITLE_MINI
                            + " <#64748B>" + server.name()));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        Account account = Core.getAccountController().of(player.getUniqueId());

        String rankDisplay = "§7MEMBER";
        if (account != null) {
            rankDisplay = account.getRank().getType().getColor() + account.getRank().getType().name();
        }

        String clanDisplay = "§7Nenhum";
        if (account != null && account.hasClan()) {
            var clan = account.getClan();
            if (clan != null) {
                clanDisplay = ClanTagFormatter.formatBracketed(clan);
            }
        }

        int online = Bukkit.getOnlinePlayers().size();

        setLine(obj, 15, ModernText.legacy("<#64748B> "));
        setLine(obj, 14, ModernText.legacy("<#C084FC>❈ <#F1F5F9>Rank: ") + rankDisplay);
        setLine(obj, 13, ModernText.legacy("<#38BDF8>◆ <#F1F5F9>Clan: ") + clanDisplay);
        setLine(obj, 12, ModernText.legacy("<!italic><" + ModernText.SUCCESS + ">⚡ <#F1F5F9>Online: <"
                + ModernText.SUCCESS + ">" + online));
        setLine(obj, 11, ModernText.legacy("<#64748B> "));
        setLine(obj, 10, ModernText.legacy("<!italic><" + ModernText.BRAND_B + ">" + LobbySettings.SERVER_ADDRESS));
        setLine(obj, 9, ModernText.legacy("<!italic><#94A3B8>Loja <" + ModernText.BRAND_B + ">"
                + LobbySettings.SERVER_STORE_URL));
    }

    public static void updateOnlineCount() {
        int online = Bukkit.getOnlinePlayers().size();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Objective obj = player.getScoreboard().getObjective(OBJECTIVE_ID);
            if (obj != null) {
                setLine(obj, 12, ModernText.legacy("<!italic><" + ModernText.SUCCESS + ">⚡ <#F1F5F9>Online: <"
                        + ModernText.SUCCESS + ">" + online));
            }
        }
    }

    private static void setLine(Objective obj, int score, String text) {
        var board = obj.getScoreboard();
        if (board == null) return;
        for (String entry : board.getEntries()) {
            if (obj.getScore(entry) != null && obj.getScore(entry).getScore() == score) {
                board.resetScores(entry);
            }
        }
        obj.getScore(text).setScore(score);
    }
}
