package com.minecraft.core.member.list.bedwars.objects.mission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public class Mission {
    
    private final String id;
    private final String name;
    private final String description;
    private final MissionType type;
    private final MissionPeriod period;
    private final int target;
    private final int xpReward;
    private final Material icon;
    
    public String getFormattedName() {
        return type.getColor() + name + " §7(#" + id + ")";
    }
    
    public String getFormattedPeriod() {
        return period.getColor() + period.getDisplayName();
    }
}
