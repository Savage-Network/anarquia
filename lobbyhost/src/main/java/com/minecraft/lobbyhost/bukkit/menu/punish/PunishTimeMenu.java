package com.minecraft.lobbyhost.bukkit.menu.punish;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.punishment.PunishContext;
import com.minecraft.lobbyhost.punishment.PunishmentRules;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.LinkedHashSet;
import java.util.Set;

public final class PunishTimeMenu extends Menu {

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

    private final Account target;
    private final PunishmentRules.Rule rule;

    public PunishTimeMenu(Player player, Account target, Menu parent, PunishmentRules.Rule rule) {
        super(player, "§8§lTempo §7| §f" + target.getName(), 5, parent);
        this.target = target;
        this.rule = rule;
    }

    @Override
    public void build() {
        clear();
        Set<String> times = new LinkedHashSet<>(rule.steps());
        times.addAll(PunishmentRules.customTimes());

        PunishContext ctx = PunishContext.of(player.getUniqueId());
        String selected = ctx != null ? ctx.time() : "-1";

        set(4, MenuItem.of(Material.CLOCK)
                .name("§f" + rule.coloredLabel())
                .lore("§7Selecione o tempo da punição", "§7Atual: §f" + PunishmentRules.formatTime(selected)));

        int idx = 0;
        for (String time : times) {
            if (idx >= SLOTS.length) break;
            boolean current = time.equalsIgnoreCase(selected);
            Material mat = time.equals("-1") ? Material.BARRIER : Material.LIME_STAINED_GLASS_PANE;
            MenuItem item = MenuItem.of(mat)
                    .name((current ? "§a▸ " : "§f") + PunishmentRules.formatTime(time))
                    .lore("§eClique para selecionar")
                    .click(p -> {
                        clickSound();
                        PunishContext c = PunishContext.of(p.getUniqueId());
                        if (c != null) c.setTime(time);
                        new PunishConfirmMenu(p, target, this).open();
                    });
            if (current) item.glow();
            set(SLOTS[idx++], item);
        }

        back(36);
        closeButton(40);
    }
}
