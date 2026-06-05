package com.minecraft.game.bukkit.menu.report;

import com.minecraft.game.Core;
import com.minecraft.game.backend.data.ReportData;
import com.minecraft.game.backend.model.PlayerReport;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Staff: lista paginada de denúncias abertas. */
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
        super(player, "§8§lReports §7| §fAbertas (p." + (page + 1) + ")", 6, null);
        this.reports = reports;
        this.page = page;
    }

    public static void openAsync(Player staff, int page) {
        ReportData data = Core.getReportData();
        if (data == null) {
            staff.sendMessage("§cSistema de reports indisponível.");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            List<PlayerReport> open = data.listOpen(100);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                if (open.isEmpty()) {
                    staff.sendMessage("§7Nenhuma denúncia aberta.");
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
                .name("§c§lDenúncias abertas")
                .lore(
                        "§7Total: §f" + reports.size(),
                        "§7Página: §f" + (page + 1),
                        "",
                        "§eClique para ver detalhes"
                ));

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, reports.size());

        int slotIdx = 0;
        for (int i = start; i < end && slotIdx < SLOTS.length; i++) {
            PlayerReport r = reports.get(i);
            int slot = SLOTS[slotIdx++];
            set(slot, MenuItem.of(Material.PAPER)
                    .name("§f#" + r.id() + " §8| §c" + r.reason().label())
                    .lore(
                            "§7" + r.reporterName() + " §8→ §f" + r.targetName(),
                            "§7Servidor: §f" + r.serverId(),
                            "§7Data: §f" + FMT.format(r.createdAt()),
                            "",
                            "§eClique para gerenciar"
                    )
                    .glow()
                    .click(p -> {
                        clickSound();
                        new ReportDetailMenu(p, r, this).open();
                    }));
        }

        if (page > 0) {
            set(45, MenuItem.of(Material.ARROW)
                    .name("§aPágina anterior")
                    .click(p -> {
                        clickSound();
                        new ReportsListMenu(p, reports, page - 1).open();
                    }));
        }
        if (end < reports.size()) {
            set(53, MenuItem.of(Material.ARROW)
                    .name("§aPróxima página")
                    .click(p -> {
                        clickSound();
                        new ReportsListMenu(p, reports, page + 1).open();
                    }));
        }

        closeButton(49);
    }
}
