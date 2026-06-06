package com.minecraft.game.account.context.objects.stats;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/** Conquistas, streak de login e progresso meta-game. */
@Getter
@Setter
public class PlayerProgress {

    private int loginStreak;
    private int totalLoginDays;
    /** Dia UTC yyyy-MM-dd do ultimo login contabilizado. */
    private String lastLoginDay = "";
    private Set<String> achievements = new HashSet<>();
}
