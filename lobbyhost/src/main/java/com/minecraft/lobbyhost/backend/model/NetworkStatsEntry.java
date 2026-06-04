package com.minecraft.lobbyhost.backend.model;

import lombok.Builder;
import lombok.Value;

/**
 * Linha da tabela {@code network_stats} — ranking global (lobby + game).
 */
@Value
@Builder
public class NetworkStatsEntry {

    public enum Category { PLAYER, CLAN }

    String statKey;
    Category category;
    String displayName;
    int kills;
    int deaths;
    int golds;
    int clanPower;
    int clanMembers;
    int bestKillStreak;

    public double kdr() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }
}
