package com.minecraft.lobbyhost.bukkit.menu.report;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.report.ReportContext;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ReportPlayerMenu extends Menu {

    private static final int PAGE_SIZE = 21;
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final Account account;
    private final int page;

    public ReportPlayerMenu(Player player, Account account, int page) {
        super(player, ModernText.menuTitle("Denúncia — Jogador"), 6, null);
        this.account = account;
        this.page = page;
    }

    @Override
    public void build() {
        clear();

        set(4, MenuItem.of(Material.COMPASS)
                .miniName(ModernText.gradient("Quem denunciar?", ModernText.ERROR, "#FCA5A5"))
                .miniLore(
                        ModernText.mini("Clique no jogador suspeito"),
                        ModernText.mini("Página " + (page + 1)),
                        "",
                        ModernText.hint("Próximo: motivo")
                ));

        List<Player> targets = new ArrayList<>();
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            targets.add(other);
        }
        targets.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, targets.size());

        if (targets.isEmpty()) {
            set(22, MenuItem.of(Material.BARRIER)
                    .miniName("<!italic><" + ModernText.ERROR + ">Ninguém online")
                    .miniLore(ModernText.mini("Não há outros jogadores")));
        } else {
            int slotIdx = 0;
            for (int i = start; i < end && slotIdx < SLOTS.length; i++) {
                Player target = targets.get(i);
                int slot = SLOTS[slotIdx++];
                set(slot, MenuItem.of(Material.PLAYER_HEAD)
                        .skullOwner(target.getName())
                        .miniName(ModernText.label(target.getName()))
                        .miniLore(ModernText.action("Clique para continuar"))
                        .click(p -> {
                            clickSound();
                            ReportContext ctx = ReportContext.of(p.getUniqueId());
                            if (ctx == null) ctx = ReportContext.open(p.getUniqueId());
                            ctx.setTargetName(target.getName());
                            new ReportReasonMenu(p, account, this).open();
                        }));
            }
        }

        if (page > 0) {
            set(45, MenuItem.of(Material.ARROW)
                    .miniName(ModernText.gradient("Anterior", ModernText.ACCENT_A, ModernText.ACCENT_B))
                    .click(p -> new ReportPlayerMenu(p, account, page - 1).open()));
        }
        if (end < targets.size()) {
            set(53, MenuItem.of(Material.ARROW)
                    .miniName(ModernText.gradient("Próxima", ModernText.ACCENT_A, ModernText.ACCENT_B))
                    .click(p -> new ReportPlayerMenu(p, account, page + 1).open()));
        }

        closeButton(49);
    }
}
