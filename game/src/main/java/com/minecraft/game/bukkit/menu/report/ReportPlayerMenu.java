package com.minecraft.game.bukkit.menu.report;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.report.ReportContext;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Passo 1: escolher jogador denunciado. */
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
        super(player, "§8§lDenúncia §7| §fEscolha o jogador", 6, null);
        this.account = account;
        this.page = page;
    }

    @Override
    public void build() {
        clear();

        set(4, MenuItem.of(Material.COMPASS)
                .name("§c§lQuem você quer denunciar?")
                .lore(
                        "§7Clique no jogador suspeito",
                        "§7Página §f" + (page + 1),
                        "",
                        "§8Próximo passo: motivo"
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
                    .name("§cNinguém online")
                    .lore("§7Não há outros jogadores no servidor"));
        } else {
            int slotIdx = 0;
            for (int i = start; i < end && slotIdx < SLOTS.length; i++) {
                Player target = targets.get(i);
                int slot = SLOTS[slotIdx++];
                set(slot, MenuItem.of(Material.PLAYER_HEAD)
                        .skullOwner(target.getName())
                        .name("§f" + target.getName())
                        .lore("§7Denunciar este jogador", "", "§eClique para continuar")
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
                    .name("§aPágina anterior")
                    .click(p -> {
                        clickSound();
                        new ReportPlayerMenu(p, account, page - 1).open();
                    }));
        }
        if (end < targets.size()) {
            set(53, MenuItem.of(Material.ARROW)
                    .name("§aPróxima página")
                    .click(p -> {
                        clickSound();
                        new ReportPlayerMenu(p, account, page + 1).open();
                    }));
        }

        closeButton(49);
    }
}
