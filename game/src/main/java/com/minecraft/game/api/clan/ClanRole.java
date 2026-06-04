package com.minecraft.game.api.clan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClanRole {

    MEMBER("Membro", "☆", "§7"),
    RECRUITER("Recrutador", "✸", "§a"),
    LEADER("Líder", "✵", "§c");

    private final String displayName;
    private final String symbol;
    private final String color;
}
