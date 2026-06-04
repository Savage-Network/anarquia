package com.minecraft.lobbyhost.bukkit.lobby;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.architect.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Scoreboard unico por jogador: sidebar + teams do {@code TagManager}.
 * Evita que tags substituam o board da sidebar (problema comum na migracao).
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
            obj = board.registerNewObjective(OBJECTIVE_ID, Criteria.DUMMY, "§2§lSAVAGE");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        Account account = Core.getAccountController().of(player.getUniqueId());
        String rankName = account != null ? account.getRank().getType().getColor()
                + account.getRank().getType().name() : "§7MEMBER";
        String clan = "§cNenhum";
        if (account != null && account.getContext().getClan() != null) {
            String tag = account.getContext().getClan().getTag();
            if (tag != null && !tag.isBlank()) clan = tag.trim();
        }

        setLine(obj, 15, "§7");
        setLine(obj, 14, "§eRank: " + rankName);
        setLine(obj, 13, "§eLobby: §7#" + server.name());
        setLine(obj, 12, "§eClan: " + clan);
        setLine(obj, 11, "§eJogadores: §f" + Bukkit.getOnlinePlayers().size());
        setLine(obj, 10, "§7 ");
        setLine(obj, 9, "§6savagemc.net");
    }

    public static void updateOnlineCount() {
        int online = Bukkit.getOnlinePlayers().size();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Objective obj = player.getScoreboard().getObjective(OBJECTIVE_ID);
            if (obj != null) {
                setLine(obj, 11, "§eJogadores: §f" + online);
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
