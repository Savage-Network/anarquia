package com.minecraft.lobbyhost.bukkit.menu.report;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.backend.data.ReportData;
import com.minecraft.lobbyhost.backend.model.PlayerReport;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ReportsListMenu extends Menu {

    private static final int PAGE_SIZE = 21;
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm")
            .withZone(ZoneId.systemDefault());

    private final List<PlayerReport> reports;
    private final int page;

    private ReportsListMenu(Player player, List<PlayerReport> reports, int page) {
        super(player, ModernText.menuTitle("Reports — Abertas"), 6, null);
        this.reports = reports;
        this.page = page;
    }

    public static void openAsync(Player staff, int page) {
        ReportData data = Core.getReportData();
        if (data == null) {
            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR + ">Reports indisponível."));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            List<PlayerReport> open = data.listOpen(100);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                if (open.isEmpty()) {
                    staff.sendMessage(ModernText.parse(ModernText.mini("Nenhuma denúncia aberta.")));
                    return;
                }
                new ReportsListMenu(staff, open, page).open();
            });
        });
    }

    @Override
    public void build() {
        clear();

        set(4, MenuItem.of(Material.WRITABLE_BOOK)
                .miniName(ModernText.gradient("Denúncias abertas", ModernText.ERROR, "#FCA5A5"))
                .miniLore(
                        ModernText.mini("Total: " + reports.size()),
                        ModernText.mini("Página " + (page + 1)),
                        ModernText.action("Clique para gerenciar")
                ));

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, reports.size());

        int slotIdx = 0;
        for (int i = start; i < end && slotIdx < SLOTS.length; i++) {
            PlayerReport r = reports.get(i);
            int slot = SLOTS[slotIdx++];
            set(slot, MenuItem.of(Material.PAPER)
                    .miniName("<#F1F5F9>#" + r.id() + " <#64748B>| " + r.reason().label())
                    .miniLore(
                            ModernText.mini(r.reporterName() + " → " + r.targetName()),
                            ModernText.mini("Servidor: " + r.serverId()),
                            ModernText.mini("Data: " + FMT.format(r.createdAt())),
                            ModernText.action("Ver detalhes")
                    )
                    .glow()
                    .click(p -> {
                        clickSound();
                        new ReportDetailMenu(p, r, this).open();
                    }));
        }

        if (page > 0) {
            set(45, MenuItem.of(Material.ARROW)
                    .miniName(ModernText.gradient("Anterior", ModernText.ACCENT_A, ModernText.ACCENT_B))
                    .click(p -> new ReportsListMenu(p, reports, page - 1).open()));
        }
        if (end < reports.size()) {
            set(53, MenuItem.of(Material.ARROW)
                    .miniName(ModernText.gradient("Próxima", ModernText.ACCENT_A, ModernText.ACCENT_B))
                    .click(p -> new ReportsListMenu(p, reports, page + 1).open()));
        }

        closeButton(49);
    }
}
