package com.minecraft.game.anticheat;

import com.minecraft.game.Core;
import com.minecraft.game.api.punishment.PunishmentCategory;
import com.minecraft.game.api.punishment.PunishmentReason;
import com.minecraft.game.punishment.PunishmentService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Punições automáticas por VL acumulado. */
public final class AnticheatPunishService {

    private static final Set<UUID> PENDING = ConcurrentHashMap.newKeySet();

    private AnticheatPunishService() {}

    public static void evaluate(Player player, int totalVl) {
        if (player == null || !AnticheatConfig.isPunishEnabled()) return;
        UUID id = player.getUniqueId();
        if (PENDING.contains(id)) return;

        if (totalVl >= AnticheatConfig.getBanVl()) {
            PENDING.add(id);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                try {
                    PunishmentService.apply(
                            null,
                            "ANTICHEAT",
                            player.getName(),
                            PunishmentCategory.BAN,
                            PunishmentReason.CHEATING,
                            "-1",
                            "Anticheat VL " + totalVl);
                } finally {
                    PENDING.remove(id);
                }
            });
            return;
        }

        if (totalVl >= AnticheatConfig.getKickVl()) {
            PENDING.add(id);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                try {
                    player.kickPlayer("§c[ANTICHEAT]\n§7Detecções suspeitas (VL: " + totalVl + ").");
                    AnticheatEngine.resetViolations(id);
                } finally {
                    PENDING.remove(id);
                }
            });
        }
    }
}
