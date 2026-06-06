package com.minecraft.game.backup;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.backend.data.AccountBackupData;
import com.minecraft.game.backend.data.AccountData;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BackupService {

    private BackupService() {}

    public static void runScheduled() {
        AccountData accountData = Core.getAccountData();
        AccountBackupData backupData = Core.getAccountBackupData();
        if (accountData == null || backupData == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            int saved = 0;
            var online = Core.getAccountController() != null
                    ? new java.util.ArrayList<>(Core.getAccountController().list()) : List.<Account>of();
            for (Account account : online) {
                if (account == null) continue;
                try {
                    String homesJson = Core.GSON.toJson(account.getHomes());
                    backupData.insert(account.getId(), account.getNickname(), "homes", homesJson);
                    String vaultJson = loadVaultJson(account.getId());
                    if (vaultJson != null && !vaultJson.isBlank()) {
                        backupData.insert(account.getId(), account.getNickname(), "vault", vaultJson);
                    }
                    saved++;
                } catch (Exception e) {
                    Core.getLogger().warning("[Backup] " + account.getNickname() + ": " + e.getMessage());
                }
            }
            int pruned = backupData.pruneOlderThanDays(14);
            Core.getLogger().info("[Backup] " + saved + " jogador(es) | removidos antigos: " + pruned);
        });
    }

    private static String loadVaultJson(UUID playerId) {
        if (Core.getPlayerVaultData() == null) return null;
        try {
            return Core.GSON.toJson(Map.of(
                    "bau", Core.getPlayerVaultData().loadBau(playerId),
                    "ec", Core.getPlayerVaultData().loadEc(playerId)));
        } catch (Exception e) {
            return null;
        }
    }
}
