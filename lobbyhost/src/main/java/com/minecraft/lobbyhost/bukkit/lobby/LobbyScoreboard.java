package com.minecraft.lobbyhost.bukkit.lobby;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.clan.ClanTagFormatter;
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
            obj = board.registerNewObjective(OBJECTIVE_ID, Criteria.DUMMY, "§b§lLOBBY " + server.name());
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        Account account = Core.getAccountController().of(player.getUniqueId());
        
        // Rank com ícone
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
        
        // Contagem de jogadores online
        int online = Bukkit.getOnlinePlayers().size();
        
        // Linha 15: espaço em branco
        setLine(obj, 15, "§7");
        
        // Linha 14: Rank com ícone
        setLine(obj, 14, "§d❈ §fRank: " + rankDisplay);
        
        // Linha 13: Clan com ícone
        setLine(obj, 13, "§3◆ §fClan: " + clanDisplay);
        
        // Linha 12: Online com ícone e destaque
        setLine(obj, 12, "§a⚡ §fOnline: §a" + online);
        
        // Linha 11: espaço em branco
        setLine(obj, 11, "§7 ");
        
        // Linha 10: Website
        setLine(obj, 10, "§esavagemc.net");
    }

    public static void updateOnlineCount() {
        int online = Bukkit.getOnlinePlayers().size();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Objective obj = player.getScoreboard().getObjective(OBJECTIVE_ID);
            if (obj != null) {
                setLine(obj, 12, "§a⚡ §fOnline: §a" + online);
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
