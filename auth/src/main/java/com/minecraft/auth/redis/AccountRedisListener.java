package com.minecraft.auth.redis;

import com.minecraft.auth.AuthPlugin;
import com.minecraft.auth.Constant;
import com.minecraft.auth.Core;
import com.minecraft.auth.account.Account;
import com.minecraft.auth.bukkit.manager.PermissionManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

/** Recarrega rank/permissoes quando outro servidor altera o banco. */
public final class AccountRedisListener extends JedisPubSub {

    private static final String BAN_KICK_MESSAGE =
            "§c§lVocê foi banido.\n§7Contate a equipe no Discord se achar que foi um erro.";

    private final AuthPlugin plugin;

    public AccountRedisListener(AuthPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessage(String channel, String message) {
        if (Constant.REDIS_NETWORK_ALERT_CHANNEL.equals(channel)) {
            String[] parts = message.split("\\|", 2);
            String body = parts.length >= 2 ? parts[1] : message;
            String title = parts.length >= 2 ? parts[0] : Constant.SERVER_NAME;
            Component c = Component.text("§6§l" + title + " §8» §f" + body.replace('&', '§'));
            Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(c)));
            return;
        }
        if (Constant.REDIS_ACCOUNT_PUNISHMENTS_CHANNEL.equals(channel)) {
            onPunishment(message);
            return;
        }
        onAccountUpdate(message);
    }

    private void onPunishment(String message) {
        String[] parts = message.split("\\|", 4);
        if (parts.length < 4) return;
        if (!"BAN".equalsIgnoreCase(parts[0].trim())) return;
        if (!"APPLY".equalsIgnoreCase(parts[3].trim())) return;
        try {
            UUID target = UUID.fromString(parts[1].trim());
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player p = Bukkit.getPlayer(target);
                if (p != null && p.isOnline()) {
                    p.kickPlayer(BAN_KICK_MESSAGE);
                }
            });
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void onAccountUpdate(String message) {
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
