package com.minecraft.lobbyhost.redis;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.display.PlayerDisplayService;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public final class AccountRedisListener extends JedisPubSub {

    private final LobbyHostPlugin plugin;

    public AccountRedisListener(LobbyHostPlugin plugin) {
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
                                TagManager.applyTagForViewer(viewer, p);
                            }
                        }
                    }))
            );
        } catch (IllegalArgumentException ignored) {
        }
    }
}
