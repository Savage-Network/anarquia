package com.minecraft.core.member.list.bedwars.objects.ranked;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RankedPoints {

    KILL(50, "Kill"),
    DEATH(-20, "Morte"),
    BED_BREAK(-50, "Cama perdida"),
    DEFEAT(-75, "Derrota"),
    WIN(125, "Vitória"),
    FINAL_KILL(100, "Final Kill");

    private final int points;
    private final String description;

    public String getFormattedPoints() {
        return (points >= 0 ? "§a+" : "§c") + points;
    }
}
