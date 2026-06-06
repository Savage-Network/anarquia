package com.minecraft.game.bukkit.menu.report;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.backend.data.ReportData;
import com.minecraft.game.backend.model.PlayerReport;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.bukkit.menu.punish.PunishMenus;
import com.minecraft.game.bukkit.vanish.VanishService;
import com.minecraft.game.report.ReportStatus;
import com.minecraft.game.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Staff: detalhes e ações sobre uma denúncia. */
public final class ReportDetailMenu extends Menu {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private final PlayerReport report;

    public ReportDetailMenu(Player player, PlayerReport report, Menu parent) {
        super(player, "§8§lReport §7| §f#" + report.id(), 5, parent);
        this.report = report;
    }

    @Override
    public void build() {
        clear();

        set(4, MenuItem.of(Material.PLAYER_HEAD)
                .skullOwner(report.targetName())
                .name("§f#" + report.id() + " §8| §c" + report.reason().label())
                .lore(
                        "§7Denunciante: §f" + report.reporterName(),
                        "§7Alvo: §f" + report.targetName(),
                        "§7Servidor: §f" + report.serverId(),
                        "§7Status: §e" + report.status().name(),
                        "§7Data: §f" + FMT.format(report.createdAt()),
                        report.details() != null && !report.details().isBlank()
                                ? "§7Detalhes: §f" + report.details() : "§7Detalhes: §8—"
                ));

        set(19, MenuItem.of(Material.ENDER_PEARL)
                .name("§b§lIr ao denunciante")
                .lore("§7Teleporta até quem reportou", "", "§eClique")
                .click(p -> teleportTo(p, report.reporterId(), report.reporterName())));

        set(21, MenuItem.of(Material.ENDER_EYE)
                .name("§d§lIr ao alvo")
                .lore("§7Teleporta até o denunciado", "§7Ativa vanish automaticamente", "", "§eClique")
                .click(p -> teleportToTarget(p, report.targetId(), report.targetName())));

        set(23, MenuItem.of(Material.IRON_SWORD)
                .name("§c§lPunir alvo")
                .lore("§7Abre menu de punição", "", "§eClique")
                .click(p -> {
                    clickSound();
                    p.closeInventory();
                    PunishMenus.open(p, report.targetName());
                }));

        set(25, MenuItem.of(Material.LIME_DYE)
                .name("§a§lMarcar resolvido")
                .lore("§7Ação tomada / punido", "", "§aClique")
                .click(p -> closeReport(p, ReportStatus.ACTIONED, "Ação tomada via menu")));

        set(31, MenuItem.of(Material.GRAY_DYE)
                .name("§7§lFechar sem ação")
                .lore("§7Denúncia inválida ou resolvida", "", "§eClique")
                .click(p -> closeReport(p, ReportStatus.CLOSED, "Fechado via menu")));

        back(40);
        closeButton(44);
    }

    private void teleportTo(Player staff, java.util.UUID targetId, String name) {
        clickSound();
        Player target = Bukkit.getPlayer(targetId);
        if (target == null || !target.isOnline()) {
            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.WARN + ">"
                    + name + " está offline."));
            return;
        }
        staff.teleport(target.getLocation());
        staff.sendMessage(ModernText.parse("<!italic><" + ModernText.SUCCESS
                + ">Teleportado até <#F1F5F9>" + target.getName() + "."));
    }

    private void teleportToTarget(Player staff, java.util.UUID targetId, String name) {
        clickSound();
        Player target = Bukkit.getPlayer(targetId);
        if (target == null || !target.isOnline()) {
            staff.sendMessage(ModernText.parse("<!italic><" + ModernText.WARN + ">"
                    + name + " está offline."));
            return;
        }
        staff.teleport(target.getLocation());
        enableVanish(staff);
        staff.sendMessage(ModernText.parse("<!italic><" + ModernText.SUCCESS
                + ">Teleportado até <#F1F5F9>" + target.getName()
                + " <#64748B>(vanish ativado)."));
    }

    private static void enableVanish(Player staff) {
        Account acc = Core.getAccountController().of(staff.getUniqueId());
        if (acc != null && VanishService.canVanish(acc)) {
            VanishService.setVanished(staff, true);
        }
    }

    private void closeReport(Player staff, ReportStatus status, String note) {
        clickSound();
        Account acc = Core.getAccountController().of(staff.getUniqueId());
        if (acc == null) return;
        ReportData data = Core.getReportData();
        if (data == null) {
            staff.sendMessage("§cReports indisponível.");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(Core.getJavaPlugin(), () -> {
            boolean ok = data.close(report.id(), acc.getId(), note, status);
            Bukkit.getScheduler().runTask(Core.getJavaPlugin(), () -> {
                if (ok) {
                    successSound();
                    staff.sendMessage(ModernText.parse("<!italic><" + ModernText.SUCCESS
                            + ">Report #" + report.id() + " fechado."));
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
