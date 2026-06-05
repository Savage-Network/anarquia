package com.minecraft.proxyhost.network;

import com.minecraft.proxyhost.Constant;
import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.ProxySettings;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.util.LegacyTexts;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

/** Staff chat cross-server via Redis. */
public final class StaffChatService {

    private StaffChatService() {}

    public static boolean send(Player sender, Account account, String message) {
        if (account == null || message == null || message.isBlank()) return false;
        if (Core.getRedis() == null || !Core.getRedis().isEnabled()) return false;

        String server = account.getCurrentServer();
        String body = message.replace('|', ' ');
        String payload = account.getColoredName() + "|" + server + "|" + body;
        Core.getRedis().publish(Constant.REDIS_PROXY_STAFFCHAT_CHANNEL, payload);
        return true;
    }

    public static void deliverFromRedis(String raw) {
        String[] parts = raw.split("\\|", 3);
        if (parts.length < 3) return;
        deliverLocal(parts[0], parts[1], parts[2]);
    }

    public static void deliverLocal(String coloredFrom, String server, String body) {
        Component formatted = LegacyTexts.component(
                ProxySettings.STAFFCHAT_PREFIX + "&f" + coloredFrom + "&7 (&f" + server + "&7): &f" + body);

        Core.getProxy().getAllPlayers().forEach(p -> {
            Account a = Core.getAccountController().of(p.getUniqueId());
            if (a != null && (a.isStaffer() || p.hasPermission("proxyhost.staffchat"))) {
                p.sendMessage(formatted);
            }
        });
        Core.getProxy().getConsoleCommandSource().sendMessage(formatted);
    }
}
