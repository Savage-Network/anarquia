package com.minecraft.game.bukkit.manager;

import lombok.Data;
import java.util.UUID;

@Data
public final class TeleportRequest {
    private final UUID sender;
    private final UUID target;
    private final TeleportRequestType type;
    private final long timestamp = System.currentTimeMillis();

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 60000L; // 60 segundos
    }
}
