package com.minecraft.core.account.context.objects.joinmessage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JoinMessageRarity {

    COMMON("Comum", "§a"),
    RARE("Rara", "§9"),
    EPIC("Épica", "§5"),
    LEGENDARY("Lendária", "§6"),
    MYTHICAL("Mítica", "§d");

    private final String name;
    private final String color;

    public String getColoredName() {
        return color + name;
    }
}
