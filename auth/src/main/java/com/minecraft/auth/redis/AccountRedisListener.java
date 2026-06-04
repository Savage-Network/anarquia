package com.minecraft.auth.redis;

import com.minecraft.auth.AuthPlugin;
import com.minecraft.auth.Core;
import com.minecraft.auth.account.Account;
import com.minecraft.auth.bukkit.manager.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

/** Recarrega rank/permissoes quando outro servidor altera o banco. */
public final class AccountRedisListener extends JedisPubSub {

    private final AuthPlugin plugin;

    public AccountRedisListener(AuthPlugin plugin) {
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
                            PermissionManager.loadPermissions(p);
                        }
                    }))
            );
        } catch (IllegalArgumentException ignored) {
        }
    }
}
