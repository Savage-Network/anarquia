package com.minecraft.proxyhost.redis;

import com.minecraft.proxyhost.Constant;
import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.api.punishment.PunishmentCategory;
import com.minecraft.proxyhost.network.BroadcastService;
import com.minecraft.proxyhost.network.StaffChatService;
import com.minecraft.proxyhost.network.TellService;
import com.minecraft.proxyhost.proxy.ProxySettingsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import redis.clients.jedis.JedisPubSub;

import java.util.Locale;
import java.util.UUID;

public final class RedisListener extends JedisPubSub {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Override
    public void onMessage(String channel, String message) {
        try {
            switch (channel) {
                case Constant.REDIS_PROXY_TELL_CHANNEL -> TellService.deliverFromRedis(message);
                case Constant.REDIS_PROXY_STAFFCHAT_CHANNEL -> StaffChatService.deliverFromRedis(message);
                case Constant.REDIS_NETWORK_ALERT_CHANNEL -> BroadcastService.deliverFromRedis(message);
                case Constant.REDIS_ACCOUNT_UPDATE_CHANNEL -> onAccountUpdate(message);
                case Constant.REDIS_PROXY_SETTINGS_CHANNEL -> onProxySettings(message);
                case Constant.REDIS_ACCOUNT_PUNISHMENTS_CHANNEL -> onPunishment(message);
                default -> Core.getLogger().debug("Redis msg ignorada do canal {}: {}", channel, message);
            }
        } catch (Throwable t) {
            Core.getLogger().warn("Erro ao processar msg Redis em {}: {}", channel, t.getMessage());
        }
    }

    private void onProxySettings(String message) {
        String trimmed = message == null ? "" : message.trim();
        if (trimmed.isEmpty()) return;

        if ("reload".equalsIgnoreCase(trimmed)) {
            ProxySettingsManager settings = ProxySettingsManager.get();
            if (settings != null) settings.reload();
            return;
        }

        if (trimmed.toLowerCase(Locale.ROOT).startsWith("eventos-access|")) {
            String[] parts = trimmed.split("\\|", 2);
            if (parts.length < 2) return;
            Boolean enabled = parseToggle(parts[1]);
            if (enabled == null) return;
            boolean changed = com.minecraft.proxyhost.routing.EventosAccessController.setEnabled(enabled);
            if (changed) {
                Core.getLogger().info("Eventos access {} via Redis.", enabled ? "ABERTO" : "FECHADO");
            }
        }
    }

    private static Boolean parseToggle(String raw) {
        if (raw == null) return null;
        String v = raw.trim().toLowerCase(Locale.ROOT);
        if ("1".equals(v) || "true".equals(v) || "on".equals(v) || "open".equals(v) || "unlock".equals(v) || "abrir".equals(v)) {
            return true;
        }
        if ("0".equals(v) || "false".equals(v) || "off".equals(v) || "close".equals(v) || "lock".equals(v) || "fechar".equals(v)) {
            return false;
        }
        return null;
    }

    private void onPunishment(String message) {
        String[] parts = message.split("\\|", 4);
        if (parts.length < 4) return;
        try {
            PunishmentCategory category = PunishmentCategory.valueOf(parts[0]);
            UUID target = UUID.fromString(parts[1]);
            if (category != PunishmentCategory.BAN) return;
            if (!"APPLY".equalsIgnoreCase(parts[3])) return;
            Core.getProxy().getPlayer(target).ifPresent(p ->
                    Core.getPunishmentData().findActive(target, PunishmentCategory.BAN).ifPresent(ban ->
                            p.disconnect(com.minecraft.proxyhost.util.DisconnectMessages.ban(ban))));
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void onAccountUpdate(String uuid) {
        try {
            UUID id = UUID.fromString(uuid.trim());
            Account online = Core.getAccountController().of(id);
            if (online == null) return;
            Core.getAccountData().loadAsync(id).thenAccept(opt ->
                    opt.ifPresent(fresh -> online.replaceContext(fresh.getContext()))
            );
        } catch (IllegalArgumentException ignored) {
        }
    }

    private Component legacy(String text) {
        return LEGACY.deserialize(text.replace('&', '§'));
    }
}
