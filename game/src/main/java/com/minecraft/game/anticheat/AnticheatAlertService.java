package com.minecraft.game.anticheat;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.GameSettings;
import com.minecraft.game.account.Account;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/** Alertas de deteccao para staff — prefixo {@link GameSettings#ANTICHEAT_ALERT_PREFIX}. */
public final class AnticheatAlertService {

    private AnticheatAlertService() {}

    public static void flag(Player suspect, String check, String detail, int checkVl, int totalVl) {
        if (suspect == null || check == null || check.isBlank()) return;
        if (!AnticheatConfig.isEnabled()) return;

        String name = suspect.getName();
        UUID id = suspect.getUniqueId();
        String info = detail == null ? "" : detail.replace('|', ' ');

        notifySuspect(suspect);
        if (!AnticheatConfig.isAlertStaff()) return;

        String payload = GameSettings.SERVER_CHAT_ID + "|" + id + "|" + name + "|" + check
                + "|" + checkVl + "|" + totalVl + "|" + info;
        if (Core.getRedis() != null && Core.getRedis().isEnabled()) {
            Core.getRedis().publish(Constant.REDIS_ANTICHEAT_ALERT_CHANNEL, payload);
        } else {
            deliverLocal(name, check, checkVl, totalVl, info, GameSettings.SERVER_CHAT_ID);
        }
    }

    /** Compatível com payload antigo (6 partes) e novo (7 partes com total VL). */
    public static void deliverFromRedis(String message) {
        String[] parts = message.split("\\|", 7);
        if (parts.length < 5) return;
        String server = parts[0];
        String check = parts[3];
        int checkVl;
        int totalVl;
        try {
            checkVl = Integer.parseInt(parts[4]);
        } catch (NumberFormatException e) {
            checkVl = 0;
        }
        String name = parts[2];
        String info;
        if (parts.length >= 7) {
            try {
                totalVl = Integer.parseInt(parts[5]);
            } catch (NumberFormatException e) {
                totalVl = checkVl;
            }
            info = parts[6];
        } else {
            totalVl = checkVl;
            info = parts.length >= 6 ? parts[5] : "";
        }
        deliverLocal(name, check, checkVl, totalVl, info, server);
    }

    private static void deliverLocal(String playerName, String check, int checkVl, int totalVl,
                                     String detail, String server) {
        var plugin = Core.getJavaPlugin();
        if (plugin == null) return;

        String summary = GameSettings.ANTICHEAT_ALERT_PREFIX
                + "§f" + playerName
                + " §7(" + server + ") §7» §e" + check
                + " §7VL:§c" + checkVl
                + " §8| §7Total:§c" + totalVl;
        Component main = Component.text(summary);
        Component verbose = detail != null && !detail.isBlank()
                ? Component.text("§8  » §7" + detail) : null;

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player staff : Bukkit.getOnlinePlayers()) {
                Account acc = Core.getAccountController().of(staff.getUniqueId());
                if (acc == null || !acc.isStaffer()) continue;
                if (!acc.getToggle().isAcStaffAlerts()) continue;
                staff.sendMessage(main);
                if (verbose != null && acc.getToggle().isAcStaffVerbose()) {
                    staff.sendMessage(verbose);
                }
            }
        });

        Core.getLogger().info("[ANTICHEAT] " + playerName + " @ " + server + " » " + check
                + " VL:" + checkVl + " total:" + totalVl
                + (detail != null && !detail.isBlank() ? " | " + detail : ""));
    }

    private static void notifySuspect(Player suspect) {
        Account acc = Core.getAccountController().of(suspect.getUniqueId());
        if (acc == null || !acc.getToggle().isAcNotifyOnFlag()) return;
        suspect.sendMessage(Component.text(
                GameSettings.ANTICHEAT_ALERT_PREFIX + "§7Comportamento suspeito detectado."));
    }
}
