package com.minecraft.game.redis;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.GamePlugin;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.punishment.PunishmentCategory;
import com.minecraft.game.punishment.PunishmentService;
import com.minecraft.game.anticheat.AnticheatAlertService;
import com.minecraft.game.report.ReportService;
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
        if (Constant.REDIS_PROXY_STAFFCHAT_CHANNEL.equals(channel)) {
            // Entrega centralizada no proxy via Velocity.
            return;
        }
        if (Constant.REDIS_NETWORK_REPORT_CHANNEL.equals(channel)) {
            Bukkit.getScheduler().runTask(plugin, () -> ReportService.deliverFromRedis(message));
            return;
        }
        if (Constant.REDIS_ANTICHEAT_ALERT_CHANNEL.equals(channel)) {
            Bukkit.getScheduler().runTask(plugin, () -> AnticheatAlertService.deliverFromRedis(message));
            return;
        }
        if (Constant.REDIS_ACCOUNT_PUNISHMENTS_CHANNEL.equals(channel)) {
            onPunishment(message);
            return;
        }
        if (Constant.REDIS_NETWORK_ALERT_CHANNEL.equals(channel)) {
            // Entrega centralizada no proxy via Velocity.
            return;
        }
        if (Constant.REDIS_ACCOUNT_UPDATE_CHANNEL.equals(channel)) {
            onAccountUpdate(message);
        }
    }

    private void onPunishment(String message) {
        String[] parts = message.split("\\|", 4);
        if (parts.length < 4) return;
        try {
            PunishmentCategory category = PunishmentCategory.valueOf(parts[0]);
            UUID target = UUID.fromString(parts[1]);
            if (!"APPLY".equalsIgnoreCase(parts[3])) return;
            if (category != PunishmentCategory.BAN) return;
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player p = Bukkit.getPlayer(target);
                if (p == null || !p.isOnline()) return;
                var data = Core.getPunishmentData();
                if (data == null) return;
                data.findActive(target, PunishmentCategory.BAN).ifPresent(ban ->
                        p.kickPlayer(PunishmentService.banKickMessage(ban)));
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
