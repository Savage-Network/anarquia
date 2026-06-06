package com.minecraft.lobbyhost.api.clan;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Metadados persistidos do clã (membros, poder, data de criação).
 */
@Getter
@Setter
public class ClanStorage {

    private final Map<UUID, ClanMember> members = new HashMap<>();
    private final Set<UUID> allies = new HashSet<>();
    private final Set<UUID> enemies = new HashSet<>();
    private long bankBalance;
    private int power;
    private long createdAt = System.currentTimeMillis();
}
