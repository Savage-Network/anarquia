package com.minecraft.core.member.list.bedwars.objects.ranked;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum RankedTier {

    ROOKIE("Rookie", "§7", 0),
    RECRUIT("Recruit", "§f", 500),
    SOLDIER("Soldier", "§a", 1000),
    VETERAN("Veteran", "§2", 1500),
    ELITE("Elite", "§b", 2000),
    GLADIATOR("Gladiator", "§5", 2500),
    TITAN("Titan", "§6", 3000),
    MASTER("Master", "§c", 4000);

    private final String name;
    private final String color;
    private final int requiredPoints;

    public String getColoredName() {
        return color + name;
    }

    public static RankedTier getByPoints(int points) {
        RankedTier[] tiers = values();
        
        for (int i = tiers.length - 1; i >= 0; i--) {
            if (points >= tiers[i].getRequiredPoints()) {
                return tiers[i];
            }
        }
        
        return ROOKIE;
    }

    public static RankedTier getByName(String name) {
        return Arrays.stream(values())
                .filter(tier -> tier.name().equalsIgnoreCase(name) || tier.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(ROOKIE);
    }

    public RankedTier getNext() {
        int nextOrdinal = ordinal() + 1;
        return nextOrdinal < values().length ? values()[nextOrdinal] : this;
    }

    public boolean isMax() {
        return this == MASTER;
    }

    public int getPointsToNext() {
        if (isMax()) return 0;
        return getNext().getRequiredPoints() - requiredPoints;
    }
}
