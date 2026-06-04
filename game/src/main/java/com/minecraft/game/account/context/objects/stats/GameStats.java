package com.minecraft.game.account.context.objects.stats;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameStats {

    private int kills;
    private int deaths;
    private int killStreak;
    private int bestKillStreak;
    /** Recompensa acumulada na cabeca (bounty). */
    private int bounty;
}
