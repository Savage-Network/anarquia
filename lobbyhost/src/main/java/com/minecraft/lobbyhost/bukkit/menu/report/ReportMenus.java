package com.minecraft.lobbyhost.bukkit.menu.report;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.report.ReportContext;
import org.bukkit.entity.Player;

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
