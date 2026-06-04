package com.minecraft.core.bungee.bot.controller;

import com.minecraft.core.api.punishment.Punishment;

/**
 * Ponte para o bot Discord standalone ({@link PunishmentNotifier}).
 * O proxy não inicia mais o Javacord embarcado.
 */
public class BotController {

    public static void sendPunishmentMessage(Punishment punishment) {
        PunishmentNotifier.notify("create", punishment);
    }

    public static void sendRevocationMessage(Punishment punishment) {
        PunishmentNotifier.notify("revoke", punishment);
    }

    public static void sendPublicBanMessage(Punishment punishment) {
        PunishmentNotifier.notify("create", punishment);
    }

    public static void sendPublicMuteMessage(Punishment punishment) {
        PunishmentNotifier.notify("create", punishment);
    }
}
