package com.minecraft.proxyhost.redis;

import com.minecraft.proxyhost.Constant;
import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.account.Account;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

/**
 * Receiver Redis para mensagens publicadas pelos backends Bukkit.
 *
 * <p>Formato esperado (JSON-ish simples, separado por pipe):
 * <ul>
 *   <li>{@code proxy-tell-channel} = {@code <senderUUID>|<targetUUID>|<senderColoredName>|<message>}</li>
 *   <li>{@code proxy-staffchat-channel} = {@code <senderColoredName>|<server>|<message>}</li>
 *   <li>{@code account-update-channel} = {@code <accountUUID>} - invalida cache</li>
 * </ul>
 */
public final class RedisListener extends JedisPubSub {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Override
    public void onMessage(String channel, String message) {
        try {
            switch (channel) {
                case Constant.REDIS_PROXY_TELL_CHANNEL -> onTell(message);
                case Constant.REDIS_PROXY_STAFFCHAT_CHANNEL -> onStaffChat(message);
                case Constant.REDIS_ACCOUNT_UPDATE_CHANNEL -> onAccountUpdate(message);
                default -> Core.getLogger().debug("Redis msg ignorada do canal {}: {}", channel, message);
            }
        } catch (Throwable t) {
            Core.getLogger().warn("Erro ao processar msg Redis em {}: {}", channel, t.getMessage());
        }
    }

    private void onTell(String message) {
        String[] parts = message.split("\\|", 4);
        if (parts.length < 4) return;
        try {
            UUID target = UUID.fromString(parts[1]);
            String coloredFrom = parts[2];
            String body = parts[3];
            Core.getProxy().getPlayer(target).ifPresent(p ->
                    p.sendMessage(legacy("&8(&fde &7" + coloredFrom + "&8) &7" + body))
            );
        } catch (IllegalArgumentException ignored) {}
    }

    private void onStaffChat(String message) {
        String[] parts = message.split("\\|", 3);
        if (parts.length < 3) return;
        String coloredFrom = parts[0];
        String server = parts[1];
        String body = parts[2];

        String prefix = Core.getPlugin().getConfig().getString("staffchat.prefix", "&6[STAFF] ");
        Component formatted = legacy(prefix + "&f" + coloredFrom + "&7 (&f" + server + "&7): &f" + body);

        Core.getProxy().getAllPlayers().forEach(p -> {
            Account a = Core.getAccountController().of(p.getUniqueId());
            if (a != null && (a.isStaffer() || p.hasPermission("proxyhost.staffchat"))) {
                p.sendMessage(formatted);
            }
        });
    }

    private void onAccountUpdate(String uuid) {
        try {
            UUID id = UUID.fromString(uuid.trim());
            Account online = Core.getAccountController().of(id);
            if (online == null) return;
            // Recarrega do banco e substitui o contexto
            Core.getAccountData().loadAsync(id).thenAccept(opt ->
                    opt.ifPresent(fresh -> online.replaceContext(fresh.getContext()))
            );
        } catch (IllegalArgumentException ignored) {}
    }

    private Component legacy(String text) {
        return LEGACY.deserialize(text.replace('&', '§'));
    }
}
