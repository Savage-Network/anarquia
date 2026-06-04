package com.minecraft.core.bukkit.menu.server.report;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.report.Report;
import com.minecraft.core.api.report.context.ReportContext;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.server.report.type.ReportInfoMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@Setter
public class ReportMenu extends Menu {

    private ReportFilter filter = ReportFilter.ALL;

    public ReportMenu(Player player) {
        super(player, "Histórico de denúncias", 5, 21);
    }

    @Override
    public void handle() {
        clear();

        List<Report> list = filter.getAction().handle(Core.getReportData().list());

        // Realizando iteração
        Iterator<Report> iterator = list.iterator();

        while (iterator.hasNext()) {
            Report report = iterator.next();

            if (report == null) {
                iterator.remove();
                return;
            }

            if (report.hasExpired()) {
                iterator.remove();

                Core.getReportData().delete(report);
            }
        }

        if (list.isEmpty())
            addItem(22, Item.of(Material.WEB, "§cNão há denúncias..."));
        else {
            // Construindo página
            buildPageItems(list, 10, (report, slot) -> {
                Account target = Core.getAccountData().of(report.getTarget());

                if (target == null) return;

                List<ReportContext> contexts = new ArrayList<>(report.getContexts());

                List<String> lore = new ArrayList<>(Collections.singletonList(
                        "§7Qtd. de denúncias: §f" + contexts.size()
                ));

                if (!contexts.isEmpty()) {
                    ReportContext last = contexts.get(contexts.size() - 1);

                    Account account = last.getAccount();

                    if (account != null)
                        lore.addAll(Arrays.asList(
                                "",
                                "§7Última denúncia:",
                                " §7Jogador: §f" + account.getNickname(),
                                " §7Motivo: §f" + last.getReason()
                        ));
                }

                lore.add("");
                lore.add("§eClique para ver!");

                final int reportSlot = slot;
                addItem(reportSlot, Item.of(Material.SKULL_ITEM, 3, (target.isOnline() ? "§a" : "§c") + target.getNickname(), lore)
                        .skullByBase64(target.getSkin().getValue())
                        .click(event -> {
                            sound(MenuSound.PAGINATED);
                            new ReportInfoMenu(getPlayer(), report, this).handle();
                        })
                        .updater(view -> {
                            if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                            Item item = getContents().get(reportSlot);

                            if (item != null) {
                                item.name((target.isOnline() ? "§a" : "§c") + target.getNickname());

                                view.setItem(reportSlot, item);

                                getPlayer().updateInventory();
                            }
                        })
                );
            });

            // Filtro de denúncias
            List<String> filterLore = new ArrayList<>(Collections.singletonList(""));

            for (ReportFilter filter : ReportFilter.values()) {
                filterLore.add((this.filter.equals(filter) ? "§a" + Constant.ARROW_ALT_SYMBOL + " " : "§7") + filter.getName());
            }

            filterLore.addAll(Arrays.asList(
                    "",
                    "§eClique para alternar!"
            ));

            addItem(getTotalSlots() - 4, Item.of(Material.HOPPER, "§bFiltrando por:", filterLore)
                    .click(event -> {
                        setFilter(filter.next());

                        sound(MenuSound.DONE);
                        handle();
                    }));
        }

        addCloseButton();

        display();
    }

    @Getter
    @AllArgsConstructor
    public enum ReportFilter {

        ALL("Todos", list -> list),
        MOST_RECENT("Mais recentes", list -> list.stream().sorted(Comparator.comparing(Report::getCreatedAt)).collect(Collectors.toList())),
        MOST_REPORT("Mais denunciado", list -> list.stream().sorted((a, b) -> Integer.compare(b.getContexts().size(), a.getContexts().size())).collect(Collectors.toList())),
        ONLINE("Onlines", list -> {
            List<Report> newList = new ArrayList<>();

            list.forEach(report -> {
                Account target = Core.getAccountData().of(report.getTarget());

                if (target != null && target.isOnline()) {
                    newList.add(report);
                }
            });

            return newList;
        }),
        A_Z("A-Z", list -> list.stream().sorted(Comparator.comparing(Report::getTargetName)).collect(Collectors.toList())),
        Z_A("Z-A", list -> list.stream().sorted(Comparator.comparing(Report::getTargetName).reversed()).collect(Collectors.toList()));

        private final String name;
        private final ReportFilterAction action;

        public interface ReportFilterAction {
            List<Report> handle(List<Report> list);
        }

        public ReportFilter next() {
            return this == Z_A ? ALL : values()[ordinal() + 1];
        }
    }
}
