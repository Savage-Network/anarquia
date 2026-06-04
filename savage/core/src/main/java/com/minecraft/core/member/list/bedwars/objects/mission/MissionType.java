package com.minecraft.core.member.list.bedwars.objects.mission;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionType {
    
    DAILY("Diária", "§a"),
    WEEKLY("Semanal", "§e"),
    PLAYER_PRESENT("Jogador presente", "§e");
    
    private final String displayName;
    private final String color;
}
