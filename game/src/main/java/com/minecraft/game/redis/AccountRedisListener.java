package com.minecraft.game.redis;

import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.display.PlayerDisplayService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

/** Recarrega conta online quando lobby/outro servidor altera o banco. */
public final class AccountRedisListener extends JedisPubSub {

    private final GamePlugin plugin;

    public AccountRedisListener(GamePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessage(String channel, String message) {
        try {
            UUID id = UUID.fromString(message.trim());
            Account online = Core.getAccountController().of(id);
            if (online == null) return;

            Core.getAccountData().loadAsync(id).thenAccept(opt ->
                    opt.ifPresent(fresh -> Bukkit.getScheduler().runTask(plugin, () -> {
                        online.replaceContext(fresh.getContext());
                        online.clearDirty();
                        Player p = Bukkit.getPlayer(id);
                        if (p != null && p.isOnline()) {
                            PlayerDisplayService.applyAll(online, p);
                            for (Player viewer : Bukkit.getOnlinePlayers()) {
                                if (viewer.equals(p)) continue;
                                com.minecraft.game.bukkit.manager.TagManager
                                        .applyTagForViewer(viewer, p);
                            }
                        }
                    }))
            );
        } catch (IllegalArgumentException ignored) {
        }
    }
}
