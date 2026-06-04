package com.minecraft.arcade.eggwars.arcade.arena.context.objects.top.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TopType {

    FINAL_KILL("Kills Finais"),
    EGG_DESTRUCTION("Ovos Destruídos");

    private final String name;
}
