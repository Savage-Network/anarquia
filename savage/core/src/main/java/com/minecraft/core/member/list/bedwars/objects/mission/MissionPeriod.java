package com.minecraft.core.member.list.bedwars.objects.mission;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionPeriod {
    
    DAILY("Diária", "§a"),
    WEEKLY("Semanal", "§e");
    
    private final String displayName;
    private final String color;
}
