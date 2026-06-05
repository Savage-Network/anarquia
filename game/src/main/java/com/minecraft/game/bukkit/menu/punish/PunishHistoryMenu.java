package com.minecraft.game.bukkit.menu.punish;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.punishment.Punishment;
import com.minecraft.game.backend.data.PunishmentData;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.util.PunishmentTimeUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public final class PunishHistoryMenu extends Menu {

    private static final int PAGE_SIZE = 21;
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final Account target;
    private final int page;

    public PunishHistoryMenu(Player player, Account target, Menu parent, int page) {
        super(player, "§8§lHistórico §7| §f" + target.getName() + " §8(p." + (page + 1) + ")", 6, parent);
        this.target = target;
        this.page = page;
    }

    @Override
    public void build() {
        clear();
        PunishmentData data = Core.getPunishmentData();
        List<Punishment> all = data != null
                ? data.listByPlayer(target.getId(), 100)
                : List.of();

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, all.size());

        set(4, MenuItem.of(Material.PLAYER_HEAD)
                .skullOwner(target.getName())
                .name("§f" + target.getName())
                .lore("§7Total: §f" + all.size() + " punições", "§7Página: §f" + (page + 1)));

        if (all.isEmpty()) {
            set(22, MenuItem.of(Material.BARRIER)
                    .name("§7Nenhuma punição")
                    .lore("§8Este jogador não possui histórico"));
        } else {
            int slotIdx = 0;
            for (int i = start; i < end && slotIdx < SLOTS.length; i++) {
                Punishment p = all.get(i);
                int slot = SLOTS[slotIdx++];
                Material mat = p.isActive() ? Material.LIME_DYE : Material.GRAY_DYE;
                String status = p.isActive() ? "§aAtiva" : "§7Revogada";
                String duration = p.isTemporary()
                        ? PunishmentTimeUtil.formatRemaining(p.getExpiresAt())
                        : "Permanente";

                set(slot, MenuItem.of(mat)
                        .name("§f" + p.getId() + " §8| §c" + p.getCategory().getDisplayName())
                        .lore(
                                "§7Status: " + status,
                                "§7Motivo: §f" + p.getReason().getDisplayName(),
                                "§7Causa: §f" + p.getCause(),
                                "§7Tempo: §f" + duration,
                                "",
                                "§eClique para detalhes"
                        )
                        .click(pl -> {
                            clickSound();
                            new PunishDetailMenu(pl, target, this, p).open();
                        }));
            }
        }

        if (page > 0) {
            set(45, MenuItem.of(Material.ARROW)
                    .name("§aPágina anterior")
                    .click(p -> {
                        clickSound();
                        new PunishHistoryMenu(p, target, parent, page - 1).open();
                    }));
        }
        if (end < all.size()) {
            set(53, MenuItem.of(Material.ARROW)
                    .name("§aPróxima página")
                    .click(p -> {
                        clickSound();
                        new PunishHistoryMenu(p, target, parent, page + 1).open();
                    }));
        }

        if (parent != null) back(48);
        closeButton(49);
    }
}
