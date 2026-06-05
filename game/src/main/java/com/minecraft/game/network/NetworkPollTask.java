package com.minecraft.game.network;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/** Atualiza o online total da rede a partir do Redis (publicado pelo proxy). */
public final class NetworkPollTask extends BukkitRunnable {

    @Override
    public void run() {
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) {
            NetworkOnlineCache.set(Bukkit.getOnlinePlayers().size());
            return;
        }

        String raw = Core.getRedis().get(Constant.REDIS_NETWORK_ONLINE_TOTAL_KEY);
        if (raw != null && !raw.isBlank()) {
            try {
                NetworkOnlineCache.set(Integer.parseInt(raw.trim()));
                return;
            } catch (NumberFormatException ignored) {
            }
        }
        NetworkOnlineCache.set(Bukkit.getOnlinePlayers().size());
    }
}
