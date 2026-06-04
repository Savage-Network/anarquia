package com.minecraft.lobbyhost.redis;

import com.minecraft.lobbyhost.Constant;
import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbyHostPlugin;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.account.context.objects.rank.RankType;
import com.minecraft.lobbyhost.bukkit.display.PlayerDisplayService;
import com.minecraft.lobbyhost.bukkit.manager.TagManager;
import com.minecraft.lobbyhost.network.NetworkStateCache;
import com.minecraft.lobbyhost.bukkit.chat.StaffChatBridge;
import com.minecraft.lobbyhost.leaderboard.LeaderboardCache;
import com.minecraft.lobbyhost.api.punishment.PunishmentCategory;
import com.minecraft.lobbyhost.punishment.PunishmentService;
import com.minecraft.lobbyhost.report.ReportService;
import com.minecraft.lobbyhost.social.PartyService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

/**
 * Subscriber unico do lobby: conta, rede (alerta/status) e party.
 */
public final class LobbyRedisSubscriber extends JedisPubSub {

    private final LobbyHostPlugin plugin;

    public LobbyRedisSubscriber(LobbyHostPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessage(String channel, String message) {
        if (channel == null) return;
        try {
            switch (channel) {
                case Constant.REDIS_ACCOUNT_UPDATE_CHANNEL -> onAccountUpdate(message);
                case Constant.REDIS_NETWORK_ALERT_CHANNEL -> onNetworkAlert(message);
                case Constant.REDIS_NETWORK_STATUS_CHANNEL -> onNetworkStatus(message);
                case Constant.REDIS_PARTY_CONNECT_CHANNEL -> onPartyConnect(message);
                case Constant.REDIS_STATS_UPDATE_CHANNEL -> onStatsUpdate();
                case Constant.REDIS_PROXY_STAFFCHAT_CHANNEL -> onStaffChat(message);
                case Constant.REDIS_PROXY_TELL_CHANNEL -> onTell(message);
                case Constant.REDIS_NETWORK_REPORT_CHANNEL -> onReport(message);
                case Constant.REDIS_ACCOUNT_PUNISHMENTS_CHANNEL -> onPunishment(message);
                default -> {}
            }
        } catch (Throwable t) {
            Core.getLogger().warning("Redis [" + channel + "]: " + t.getMessage());
        }
    }

    private void onPunishment(String message) {
        String[] parts = message.split("\\|", 4);
        if (parts.length < 4) return;
        try {
            PunishmentCategory category = PunishmentCategory.valueOf(parts[0]);
            UUID target = UUID.fromString(parts[1]);
            if (!"APPLY".equalsIgnoreCase(parts[3])) return;
            if (category == PunishmentCategory.BAN) {
                Player p = Bukkit.getPlayer(target);
                if (p == null || !p.isOnline()) return;
                var data = Core.getPunishmentData();
                if (data == null) return;
                data.findActive(target, PunishmentCategory.BAN).ifPresent(ban ->
                        Bukkit.getScheduler().runTask(plugin, () ->
                                p.kickPlayer(PunishmentService.banKickMessage(ban))));
            }
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
                                TagManager.applyTagForViewer(viewer, p);
                            }
                        }
                    }))
            );
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void onNetworkAlert(String message) {
        String[] parts = message.split("\\|", 2);
        String body = parts.length >= 2 ? parts[1] : message;
        String title = parts.length >= 2 ? parts[0] : Constant.SERVER_NAME;
        Component c = Component.text("§6§l" + title + " §8» §f" + body.replace('&', '§'));
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(c)));
    }

    private void onNetworkStatus(String message) {
        String[] parts = message.split("\\|");
        boolean maint = "1".equals(parts[0].trim()) || "true".equalsIgnoreCase(parts[0].trim());
        RankType min = RankType.HELPER;
        if (parts.length >= 2) {
            try {
                min = RankType.valueOf(parts[1].trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        NetworkStateCache.setMaintenance(maint, min);
    }

    private void onPartyConnect(String message) {
        Bukkit.getScheduler().runTask(plugin, () -> PartyService.onPartyConnectMessage(message));
    }

    private void onStatsUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, LeaderboardCache::refreshAll);
    }

    private void onStaffChat(String message) {
        String[] parts = message.split("\\|", 3);
        if (parts.length < 3) return;
        StaffChatBridge.deliverFromRedis(parts[0], parts[1], parts[2]);
    }

    private void onReport(String message) {
        Bukkit.getScheduler().runTask(plugin, () -> ReportService.deliverFromRedis(message));
    }

    private void onTell(String message) {
        String[] parts = message.split("\\|", 4);
        if (parts.length < 4) return;
        try {
            UUID target = UUID.fromString(parts[1].trim());
            String from = parts[2];
            String body = parts[3];
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player p = Bukkit.getPlayer(target);
                if (p != null && p.isOnline()) {
                    p.sendMessage(Component.text("§8(§fde §7" + from + "§8) §7" + body));
                }
            });
        } catch (IllegalArgumentException ignored) {
        }
    }
}
