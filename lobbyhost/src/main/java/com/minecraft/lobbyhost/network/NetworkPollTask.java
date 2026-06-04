package com.minecraft.lobbyhost.network;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import org.bukkit.scheduler.BukkitRunnable;

public final class NetworkPollTask extends BukkitRunnable {

    @Override
    public void run() {
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) return;
        for (LobbySettings.NetworkMode mode : LobbySettings.NETWORK_MODES) {
            String raw = Core.getRedis().get(Constant.REDIS_NETWORK_ONLINE_KEY_PREFIX + mode.heartbeatId());
            int online = 0;
            if (raw != null) {
                try {
                    online = Integer.parseInt(raw.trim());
                } catch (NumberFormatException ignored) {
                }
            }
            NetworkStateCache.setOnline(mode.heartbeatId(), online);
        }
    }
}
