package com.minecraft.proxyhost.network;

import com.minecraft.proxyhost.Core;
import com.minecraft.proxyhost.ProxySettings;
import com.minecraft.proxyhost.account.Account;
import com.minecraft.proxyhost.util.LegacyTexts;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

/** Staff chat cross-server via plugin messaging do Velocity (sem Redis). */
public final class StaffChatService {

    private StaffChatService() {}

    public static boolean send(Player sender, Account account, String message) {
        if (account == null || message == null || message.isBlank()) return false;

        String server = account.getCurrentServer();
        if (server == null || server.isBlank()) server = "proxy";
        String body = message.replace('|', ' ');
        deliverLocal(account.getColoredName(), server, body);
        return true;
    }

    /** Compatibilidade com backends antigos que ainda publicam no Redis. */
    public static void deliverFromRedis(String raw) {
        deliverFromPayload(raw);
    }

    public static void deliverFromPayload(String raw) {
        if (raw == null || raw.isBlank()) return;
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
