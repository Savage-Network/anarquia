package com.minecraft.game.bukkit.poll;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Sessao ativa de uma votacao. */
@Getter
public final class VoteSession {

    private final UUID id = UUID.randomUUID();
    private final UUID creatorId;
    private final String creatorName;
    private final List<String> options;
    private final long endsAtMs;
    private final int durationSec;

    private final Map<UUID, Integer> playerVotes = new ConcurrentHashMap<>();
    private final int[] counts;

    public VoteSession(UUID creatorId, String creatorName, List<String> options, int durationSec) {
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.options = List.copyOf(options);
        this.durationSec = durationSec;
        this.endsAtMs = System.currentTimeMillis() + durationSec * 1000L;
        this.counts = new int[options.size()];
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= endsAtMs;
    }

    public int secondsLeft() {
        return Math.max(0, (int) ((endsAtMs - System.currentTimeMillis()) / 1000L));
    }

    public synchronized boolean vote(UUID playerId, int optionIndex) {
        if (optionIndex < 0 || optionIndex >= options.size()) return false;
        if (isExpired()) return false;

        Integer previous = playerVotes.put(playerId, optionIndex);
        if (previous != null && previous >= 0 && previous < counts.length) {
            counts[previous] = Math.max(0, counts[previous] - 1);
        }
        counts[optionIndex]++;
        return true;
    }

    public int count(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= counts.length) return 0;
        return counts[optionIndex];
    }

    public int totalVotes() {
        int sum = 0;
        for (int c : counts) sum += c;
        return sum;
    }

    public Integer choiceOf(UUID playerId) {
        return playerVotes.get(playerId);
    }

    public List<String> resultLines() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            lines.add(options.get(i) + ": " + counts[i]);
        }
        return lines;
    }

    public int winningIndex() {
        int best = 0;
        for (int i = 1; i < counts.length; i++) {
            if (counts[i] > counts[best]) best = i;
        }
        return best;
    }
}
