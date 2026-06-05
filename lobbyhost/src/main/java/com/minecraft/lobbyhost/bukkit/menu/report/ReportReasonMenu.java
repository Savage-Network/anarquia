package com.minecraft.lobbyhost.bukkit.menu.report;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.report.ReportContext;
import com.minecraft.lobbyhost.report.ReportReason;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class ReportReasonMenu extends Menu {

    private static final int[] REASON_SLOTS = {20, 21, 22, 23, 24};
    private static final Material[] REASON_ICONS = {
            Material.IRON_SWORD, Material.WRITABLE_BOOK, Material.BARRIER,
            Material.SPIDER_EYE, Material.PAPER
    };

    private final Account account;

    public ReportReasonMenu(Player player, Account account, Menu parent) {
        super(player, ModernText.menuTitle("Denúncia — Motivo"), 5, parent);
        this.account = account;
    }

    @Override
    public void build() {
        clear();
        ReportContext ctx = ReportContext.of(player.getUniqueId());
        String target = ctx != null ? ctx.targetName() : "?";

        set(4, MenuItem.of(Material.PLAYER_HEAD)
                .skullOwner(target)
                .miniName(ModernText.label(target))
                .miniLore(ModernText.mini("Escolha o motivo abaixo")));

        ReportReason[] reasons = ReportReason.values();
        for (int i = 0; i < reasons.length && i < REASON_SLOTS.length; i++) {
            ReportReason reason = reasons[i];
            set(REASON_SLOTS[i], MenuItem.of(REASON_ICONS[i])
                    .miniName(reason.displayMini())
                    .miniLore(ModernText.action("Clique para continuar"))
                    .click(p -> {
                        clickSound();
                        ReportContext session = ReportContext.of(p.getUniqueId());
                        if (session == null) {
                            p.closeInventory();
                            return;
                        }
                        session.setReason(reason);
                        new ReportConfirmMenu(p, account, this).open();
                    }));
        }

        back(40);
        closeButton(44);
    }
}
