package com.minecraft.lobbyhost.anticheat;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.LobbySettings;
import com.minecraft.lobbyhost.account.Account;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** Recebe alertas de anticheat da rede (Redis) com prefixo {@link LobbySettings#ANTICHEAT_ALERT_PREFIX}. */
public final class AnticheatAlertService {

    private AnticheatAlertService() {}

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

        String summary = LobbySettings.ANTICHEAT_ALERT_PREFIX
                + "§f" + name
                + " §7(" + server + ") §7» §e" + check
                + " §7VL:§c" + checkVl
                + " §8| §7Total:§c" + totalVl;
        Component main = Component.text(summary);
        Component verbose = info != null && !info.isBlank()
                ? Component.text("§8  » §7" + info) : null;

        for (Player staff : Bukkit.getOnlinePlayers()) {
            Account acc = Core.getAccountController().of(staff.getUniqueId());
            if (acc == null || !acc.isStaffer()) continue;
            if (!acc.getToggle().isAcStaffAlerts()) continue;
            staff.sendMessage(main);
            if (verbose != null && acc.getToggle().isAcStaffVerbose()) {
                staff.sendMessage(verbose);
            }
        }

        Core.getLogger().info("[ANTICHEAT] " + name + " @ " + server + " » " + check
                + " VL:" + checkVl + " total:" + totalVl
                + (info != null && !info.isBlank() ? " | " + info : ""));
    }
}
