package com.minecraft.core.server.payload;

import com.minecraft.core.server.payload.whitelist.ServerWhitelist;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ServerPayload {

    private long heartBeat = System.currentTimeMillis();

    private int players;
    private final int maxPlayers;

    private final ServerWhitelist whitelist = new ServerWhitelist();

    private final long startedAt = System.currentTimeMillis();

    public boolean isFull() {
        return players + 1 >= maxPlayers;
    }
}
