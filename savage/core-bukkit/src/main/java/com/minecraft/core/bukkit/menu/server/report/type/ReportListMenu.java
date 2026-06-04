package com.minecraft.core.bukkit.menu.server.report.type;

import com.minecraft.core.account.Account;
import com.minecraft.core.api.report.Report;
import com.minecraft.core.api.report.context.ReportContext;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.util.list.DateUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReportListMenu extends Menu {

    private final Report report;

    public ReportListMenu(Player player, Report report, Menu last) {
        super(player, "Denúncias: " + report.getTargetName(), last, 6, 21);

        this.report = report;
    }

    @Override
    public void handle() {
        clear();

        List<ReportContext> list = new ArrayList<>(report.getContexts());

        // Iterando sobre contextos
        Iterator<ReportContext> iterator = list.iterator();

        while (iterator.hasNext()) {
            ReportContext context = iterator.next();

            if (context == null) {
                iterator.remove();
                return;
            }

            if (!context.isValid()) {
                iterator.remove();

                report.removeContext(context);
            }
        }

        if (list.isEmpty())
            addItem(22, Item.of(Material.WEB, "§cNão há motivos..."));
        else {
            // Construindo página
            buildPageItems(list, 10, (context, slot) -> {
                Account account = context.getAccount();

                if (account == null) return;

                addItem(slot, Item.of(Material.SKULL_ITEM, 3, "§a" + account.getNickname(),
                                "§7Motivo: §f" + context.getReason(),
                                "§7Data: §f" + DateUtil.getSimpleDate(context.getTimestamp()),
                                "",
                                "§7Expira em: §f" + DateUtil.getSimpleDate(context.getExpiresAt()))
                        .skullByBase64(account.getSkin().getValue()));
            });
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
