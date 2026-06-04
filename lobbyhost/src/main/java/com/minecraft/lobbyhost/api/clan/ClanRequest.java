package com.minecraft.lobbyhost.api.clan;

import lombok.Getter;

import java.util.UUID;

@Getter
public class ClanRequest {

    private final UUID clanId;
    private final UUID author;
    private final UUID receiver;
    private final long sentAt;

    public ClanRequest(UUID clanId, UUID author, UUID receiver) {
        this.clanId = clanId;
        this.author = author;
        this.receiver = receiver;
        this.sentAt = System.currentTimeMillis();
    }

    public String key() {
        return clanId + ":" + author + ":" + receiver;
    }
}
