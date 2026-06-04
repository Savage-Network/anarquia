package com.minecraft.arcade.event.game.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GameState {

    WAITING(0),
    STARTING(60),
    PLAYING(0),
    ENDING(15);

    private final int defaultTime;
}
