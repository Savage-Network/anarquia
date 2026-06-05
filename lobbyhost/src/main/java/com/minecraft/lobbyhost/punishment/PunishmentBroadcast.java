package com.minecraft.lobbyhost.punishment;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.punishment.Punishment;
import com.minecraft.lobbyhost.util.PunishmentTimeUtil;

/** Notifica staff online sobre punições aplicadas. */
public final class PunishmentBroadcast {

    private PunishmentBroadcast() {}

    public static void staff(Punishment punishment, String targetName, String authorName) {
        if (punishment == null) return;
        String duration = punishment.isTemporary()
                ? PunishmentTimeUtil.formatRemaining(punishment.getExpiresAt())
                : "Permanente";
        String msg = "§8[§cPunição§8] §e" + authorName
                + " §7→ §f" + targetName
                + " §8| §c" + punishment.getCategory().getDisplayName()
                + " §8| §7" + punishment.getReason().getDisplayName()
                + " §8| §f" + duration
                + " §8| §7" + punishment.getCause()
                + " §8| §8" + punishment.getId();

        for (Account account : Core.getAccountController().list()) {
            if (account.isStaffer()) {
                account.send(msg);
            }
        }
        Core.getLogger().info("[punish-broadcast] " + strip(msg));
    }

    public static void kick(String targetName, String authorName, String reason) {
        String msg = "§8[§cKick§8] §e" + authorName + " §7expulsou §f" + targetName + " §8| §7" + reason;
        for (Account account : Core.getAccountController().list()) {
            if (account.isStaffer()) account.send(msg);
        }
    }

    private static String strip(String s) {
        return s.replaceAll("§.", "");
    }
}
