package com.minecraft.lobbyhost.bukkit.menu.punish;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.punishment.PunishContext;
import com.minecraft.lobbyhost.punishment.PunishmentService;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** Abre o fluxo GUI de punições. */
public final class PunishMenus {

    private PunishMenus() {}

    public static void open(Player staff, String targetName) {
        if (staff == null || targetName == null || targetName.isBlank()) return;

        PunishContext.open(staff.getUniqueId(), targetName);
        PunishmentService.resolveTarget(targetName).thenAccept(opt -> Bukkit.getScheduler().runTask(
                Core.getJavaPlugin(), () -> {
                    if (opt.isEmpty()) {
                        PunishContext.close(staff.getUniqueId());
                        staff.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR
                                + ">Jogador não encontrado."));
                        return;
                    }
                    Account target = opt.get();
                    new PunishHubMenu(staff, target).open();
                }));
    }

    public static void openHistory(Player staff, String targetName) {
        if (staff == null || targetName == null || targetName.isBlank()) return;

        PunishContext.open(staff.getUniqueId(), targetName);
        PunishmentService.resolveTarget(targetName).thenAccept(opt -> Bukkit.getScheduler().runTask(
                Core.getJavaPlugin(), () -> {
                    if (opt.isEmpty()) {
                        PunishContext.close(staff.getUniqueId());
                        staff.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR
                                + ">Jogador não encontrado."));
                        return;
                    }
                    new PunishHistoryMenu(staff, opt.get(), null, 0).open();
                }));
    }
}
