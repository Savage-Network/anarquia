package com.minecraft.lobbyhost.bukkit.menu.report;

import com.minecraft.lobbyhost.Core;
import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.backend.data.ReportData;
import com.minecraft.lobbyhost.backend.model.PlayerReport;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.bukkit.menu.punish.PunishMenus;
import com.minecraft.lobbyhost.report.ReportStatus;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class ReportDetailMenu extends Menu {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private final PlayerReport report;

    public ReportDetailMenu(Player player, PlayerReport report, Menu parent) {
        super(player, ModernText.menuTitle("Report #" + report.id()), 5, parent);
        this.report = report;
    }

    @Override
    public void build() {
        clear();

        set(4, MenuItem.of(Material.PLAYER_HEAD)
                .skullOwner(report.targetName())
                .miniName(ModernText.gradient("#" + report.id(), ModernText.ERROR, "#FCA5A5"))
                .miniLore(
                        ModernText.mini("De: " + report.reporterName() + " → " + report.targetName()),
                        ModernText.mini("Motivo: " + report.reason().label()),
                        ModernText.mini("Servidor: " + report.serverId()),
                        ModernText.mini("Status: " + report.status().name()),
                        ModernText.mini("Data: " + FMT.format(report.createdAt())),
                        report.details() != null && !report.details().isBlank()
                                ? ModernText.mini("Detalhes: " + report.details())
                                : ModernText.mini("Detalhes: —")
                ));

        set(19, MenuItem.of(Material.ENDER_PEARL)
                .miniName(ModernText.gradient("Ir ao denunciante", ModernText.ACCENT_A, ModernText.ACCENT_B))
                .miniLore(ModernText.action("Teleportar"))
                .click(p -> teleportTo(p, report.reporterId(), report.reporterName())));

        set(21, MenuItem.of(Material.ENDER_EYE)
                .miniName(ModernText.gradient("Ir ao alvo", ModernText.ACCENT_A, ModernText.ACCENT_B))
                .miniLore(ModernText.action("Teleportar"))
                .click(p -> teleportTo(p, report.targetId(), report.targetName())));

        set(23, MenuItem.of(Material.IRON_SWORD)
                .miniName(ModernText.gradient("Punir alvo", ModernText.ERROR, "#FCA5A5"))
                .miniLore(ModernText.action("Abrir punições"))
                .click(p -> {
                    clickSound();
                    p.closeInventory();
                    PunishMenus.open(p, report.targetName());
                }));

        set(25, MenuItem.of(Material.LIME_DYE)
                .miniName(ModernText.gradient("Marcar resolvido", ModernText.SUCCESS, "#86EFAC"))
                .miniLore(ModernText.action("Ação tomada"))
                .click(p -> closeReport(p, ReportStatus.ACTIONED, "Ação tomada via menu")));

        set(31, MenuItem.of(Material.GRAY_DYE)
                .miniName("<!italic><" + ModernText.MUTED + ">Fechar sem ação")
                .miniLore(ModernText.action("Inválido / resolvido"))
                .click(p -> closeReport(p, ReportStatus.CLOSED, "Fechado via menu")));

        back(40);
        closeButton(44);
    }

    private void teleportTo(Player staff, java.util.UUID targetId, String name) {
        clickSound();
        Player target = Bukkit.getPlayer(targetId);
        if (target == null || !target.isOnline()) {
            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.WARN + ">" + name + " offline."));
            return;
        }
        staff.teleport(target.getLocation());
        staff.sendMessage(ModernText.parse("<!italic><" + ModernText.SUCCESS + ">Teleportado até "
                + target.getName() + "."));
    }

    private void closeReport(Player staff, ReportStatus status, String note) {
        clickSound();
        Account acc = Core.getAccountController().of(staff.getUniqueId());
        if (acc == null) return;
        ReportData data = Core.getReportData();
        if (data == null) {
            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.ERROR + ">Reports indisponível."));
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            boolean ok = data.close(report.id(), acc.getId(), note, status);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                if (ok) {
                    successSound();
                    staff.sendMessage(ModernText.parse("<!italic><" + ModernText.SUCCESS + ">Report #"
                            + report.id() + " fechado."));
                    staff.closeInventory();
                    ReportsListMenu.openAsync(staff, 0);
                } else {
                    staff.sendMessage(ModernText.parse("<!italic><" + ModernText.WARN
                            + ">Report já estava fechado."));
                }
            });
        });
    }
}
