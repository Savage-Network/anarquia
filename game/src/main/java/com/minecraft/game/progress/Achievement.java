package com.minecraft.game.progress;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Achievement {

    FIRST_BLOOD("first_blood", "Primeira Sangue", "Elimine seu primeiro jogador.", 50),
    KILLER_10("killer_10", "Caçador", "Alcance 10 eliminações.", 100),
    KILLER_100("killer_100", "Assassino", "Alcance 100 eliminações.", 500),
    STREAK_3("streak_3", "Em Chamas", "3 kills seguidas.", 75),
    STREAK_5("streak_5", "Imparável", "5 kills seguidas.", 150),
    STREAK_10("streak_10", "Lendário", "10 kills seguidas.", 400),
    LOGIN_7("login_7", "Fiel", "7 dias de login seguidos.", 200),
    LOGIN_30("login_30", "Veterano", "30 dias de login seguidos.", 1000),
    CLAN_FOUNDER("clan_founder", "Fundador", "Crie um clã.", 100),
    BOUNTY_HUNTER("bounty_hunter", "Caçador de Recompensas", "Colete uma bounty.", 150);

    private final String id;
    private final String title;
    private final String description;
    private final int goldReward;
}
