package com.minecraft.lobbyhost.account.context.objects.stats;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameStats {

    private int kills;
    private int deaths;
    private int killStreak;
    private int bestKillStreak;
    private int bounty;

    /** Livro de boas-vindas do modo GAME (persistido pelo game.jar). */
    private boolean welcomeBookGiven;
}
