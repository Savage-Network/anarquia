package com.minecraft.core.bukkit.menu.server.report.type;

import com.minecraft.core.Core;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.api.report.Report;
import com.minecraft.core.backend.database.redis.message.types.account.AccountExecuteCommandMessage;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ReportInfoMenu extends Menu {

    private final Report report;

    public ReportInfoMenu(Player player, Report report, Menu last) {
        super(player, "Denúncia: " + report.getTargetName(), last, 4);

        this.report = report;
    }

    @Override
    public void handle() {
        clear();

        addItem(11, Item.of(Material.ENDER_PEARL, "§aTeleportar",
                        "§7Vá para o local",
                        "§7do jogador.",
                        "",
                        "§eClique para ir!")
                .click(event -> {
                    close();
                    sound(MenuSound.DONE);

                    new AccountExecuteCommandMessage(getPlayer().getUniqueId(), "go " + report.getTargetName()).send();
                }));

        addItem(13, Item.of(Material.PAPER, "§aDenúncias",
                        "§7Veja as denúncias",
                        "§7que o jogador recebeu.",
                        "",
                        "§eClique para ver!")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new ReportListMenu(getPlayer(), report, this).handle();
                }));

        addItem(15, Item.of(Material.BARRIER, "§cExcluir",
                        "§eClique para remover!")
                .click(event -> {
                    sound(MenuSound.DONE);

                    Core.getReportData().delete(report);

                    if (isReturnable())
                        getLast().handle();
                }));

        if (isReturnable())
            addBackButton();

        display();
    }
}
