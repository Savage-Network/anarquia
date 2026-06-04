package com.minecraft.hungergames.game.stage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GameStage {

    WAITING(-1),

    STARTING(1),

    INVINCIBILITY(2),

    PLAYING(-1),

    DEATHMATCH(1),

    ENDING(1);


    private final int defaultMinutes;

}
