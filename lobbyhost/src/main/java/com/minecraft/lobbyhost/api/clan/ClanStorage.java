package com.minecraft.lobbyhost.api.clan;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Metadados persistidos do clã (membros, poder, data de criação).
 */
@Getter
@Setter
public class ClanStorage {

    private final Map<UUID, ClanMember> members = new HashMap<>();
    private int power;
    private long createdAt = System.currentTimeMillis();
}
