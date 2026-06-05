package com.minecraft.game.bukkit.menu.punish;

import com.minecraft.game.account.Account;
import com.minecraft.game.api.punishment.PunishmentCategory;
import com.minecraft.game.api.punishment.PunishmentReason;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.punishment.PunishContext;
import com.minecraft.game.punishment.PunishmentPresets;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public final class PunishPresetMenu extends Menu {

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

    private final Account target;
    private final PunishmentCategory category;
    private final PunishmentReason reason;

    public PunishPresetMenu(Player player, Account target, Menu parent,
                            PunishmentCategory category, PunishmentReason reason) {
        super(player, "§8§lPresets §7| §f" + target.getName(), 5, parent);
        this.target = target;
        this.category = category;
        this.reason = reason;
    }

    @Override
    public void build() {
        clear();
        List<PunishmentPresets.Entry> presets = PunishmentPresets.filter(category, reason);

        set(4, MenuItem.of(Material.CLOCK)
                .name("§f" + category.getDisplayName() + " §8| §7" + reason.getDisplayName())
                .lore("§7Selecione um preset abaixo"));

        int idx = 0;
        for (PunishmentPresets.Entry entry : presets) {
            if (idx >= SLOTS.length) break;
            Material mat = category == PunishmentCategory.BAN ? Material.RED_STAINED_GLASS_PANE
                    : Material.YELLOW_STAINED_GLASS_PANE;
            int slot = SLOTS[idx++];
            set(slot, MenuItem.of(mat)
                    .name(entry.icon() + entry.label())
                    .lore(
                            "§7Tempo: §f" + PunishmentPresets.formatTime(entry.time()),
                            "§7Motivo: §f" + entry.label(),
                            "",
                            "§eClique para confirmar"
                    )
                    .click(p -> {
                        clickSound();
                        PunishContext ctx = PunishContext.of(p.getUniqueId());
                        if (ctx != null) {
                            ctx.setCategory(entry.category());
                            ctx.setReason(entry.reason());
                            ctx.setTime(entry.time());
                            ctx.setCause(entry.label());
                            ctx.setKickMode(false);
                        }
                        new PunishConfirmMenu(p, target, this).open();
                    }));
        }

        back(36);
        closeButton(40);
    }
}
