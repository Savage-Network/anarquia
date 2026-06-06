package com.minecraft.eventos.engine;

import com.minecraft.eventos.game.team.TeamPresetType;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Estado mutavel de uma sessao (times, kills, fila, participantes). */
@Getter
public final class EventContext {

    private final Map<UUID, TeamPresetType> teams = new HashMap<>();
    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Set<UUID> participants = new HashSet<>();
    private final Set<UUID> queue = new HashSet<>();
    @Setter private int blockPartyRound;
    @Setter private String blockPartySafeColor = "red";
    private final Set<org.bukkit.util.BlockVector> blockPartyRemoved = new HashSet<>();

    public void reset() {
        teams.clear();
        kills.clear();
        participants.clear();
        queue.clear();
        blockPartyRound = 0;
        blockPartySafeColor = "red";
        blockPartyRemoved.clear();
    }

    public int killsOf(UUID id) {
        return kills.getOrDefault(id, 0);
    }

    public void addKill(UUID id) {
        kills.merge(id, 1, Integer::sum);
    }

    public void registerParticipant(UUID id) {
        participants.add(id);
    }

    public boolean isParticipant(UUID id) {
        return participants.contains(id);
    }
}
