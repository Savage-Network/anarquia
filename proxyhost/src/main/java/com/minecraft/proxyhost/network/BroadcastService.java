package com.minecraft.proxyhost.network;

import com.minecraft.proxyhost.Constant;
import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.util.LegacyTexts;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/** Anúncios globais da rede (proxy + backends via Redis). */
public final class BroadcastService {

    public static final String TYPE_BROADCAST = "BROADCAST";
    public static final String TYPE_ALERT = "ALERT";

    private static final LegacyComponentSerializer LEGACY_HEX = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private BroadcastService() {}

    /** &#06402B&lSAVAGE &b➤ &f<mensagem> */
    public static Component formatBroadcast(String message) {
        String body = sanitize(message);
        return LEGACY_HEX.deserialize("§x§0§6§4§0§2§B§lSAVAGE §b➤ §f" + body.replace('&', '§'));
    }

    public static Component formatAlert(String sender, String message) {
        return LegacyTexts.component("&6&l" + Constant.SERVER_NAME + " &8» &f" + sanitize(message));
    }

    public static void sendBroadcast(String message) {
        publish(TYPE_BROADCAST, sanitize(message));
    }

    public static void sendAlert(String sender, String message) {
        publish(TYPE_ALERT, sender + "|" + sanitize(message));
    }

    public static void deliverFromRedis(String raw) {
        if (raw == null || raw.isBlank()) return;
        int sep = raw.indexOf('|');
        String type = sep > 0 ? raw.substring(0, sep) : TYPE_ALERT;
        String payload = sep > 0 ? raw.substring(sep + 1) : raw;

        if (TYPE_BROADCAST.equalsIgnoreCase(type)) {
            deliverOnProxy(formatBroadcast(payload));
            return;
        }

        if (TYPE_ALERT.equalsIgnoreCase(type)) {
            String[] parts = payload.split("\\|", 2);
            String sender = parts.length >= 2 ? parts[0] : Constant.SERVER_NAME;
            String body = parts.length >= 2 ? parts[1] : payload;
            deliverOnProxy(formatAlert(sender, body));
        }
    }

    private static void deliverOnProxy(Component message) {
        Core.getProxy().getAllPlayers().forEach(p -> p.sendMessage(message));
        Core.getProxy().getConsoleCommandSource().sendMessage(message);
    }

    private static void publish(String type, String payload) {
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) return;
        Core.getRedis().publish(Constant.REDIS_NETWORK_ALERT_CHANNEL, type + "|" + payload);
    }

    private static String sanitize(String message) {
        return message == null ? "" : message.replace('|', ' ');
    }
}
