package com.minecraft.game.bukkit.menu.report;

import com.minecraft.game.account.Account;
import com.minecraft.game.report.ReportContext;
import org.bukkit.entity.Player;

/** Abre o fluxo GUI de denúncias. */
public final class ReportMenus {

    private ReportMenus() {}

    public static void open(Player player, Account account) {
        if (player == null || account == null) return;
        ReportContext.open(player.getUniqueId());
        new ReportPlayerMenu(player, account, 0).open();
    }

    public static void openStaffList(Player staff) {
        if (staff == null) return;
        ReportsListMenu.openAsync(staff, 0);
    }
}
