package com.minecraft.eventos.redis;

import com.minecraft.eventos.Core;
import com.minecraft.eventos.EventosPlugin;
import com.minecraft.eventos.account.Account;
import com.minecraft.eventos.bukkit.display.AboveNameDisplay;
import com.minecraft.eventos.bukkit.manager.TagManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

/** Recarrega rank/tag/conta quando outro servidor altera o banco. */
public final class AccountRedisListener extends JedisPubSub {

    private final EventosPlugin plugin;

    public AccountRedisListener(EventosPlugin plugin) {
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
                        if (p == null || !p.isOnline()) return;
                        TagManager.updateTag(online);
                        TagManager.updateAllForViewer(p);
                        AboveNameDisplay.refresh(p);
                        for (Player viewer : Bukkit.getOnlinePlayers()) {
                            if (viewer.equals(p)) continue;
                            TagManager.applyTagForViewer(viewer, p);
                        }
                    }))
            );
        } catch (IllegalArgumentException ignored) {
        }
    }
}
