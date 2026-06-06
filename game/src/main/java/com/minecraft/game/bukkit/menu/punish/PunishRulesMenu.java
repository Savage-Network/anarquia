package com.minecraft.game.bukkit.menu.punish;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.punishment.PunishmentCategory;
import com.minecraft.game.backend.data.PunishmentData;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.punishment.PunishContext;
import com.minecraft.game.punishment.PunishmentRules;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class PunishRulesMenu extends Menu {

    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final Account target;
    private final PunishmentCategory category;

    public PunishRulesMenu(Player player, Account target, Menu parent, PunishmentCategory category) {
        super(player, "§8§lRegras §7| §f" + target.getName(), 6, parent);
        this.target = target;
        this.category = category;
    }

    @Override
    public void build() {
        clear();
        List<PunishmentRules.Rule> rules = PunishmentRules.forCategory(category);
        PunishmentData data = Core.getPunishmentData();

        set(4, MenuItem.of(category == PunishmentCategory.BAN ? org.bukkit.Material.RED_BANNER
                        : category == PunishmentCategory.MUTE ? org.bukkit.Material.PAPER
                        : org.bukkit.Material.OAK_SIGN)
                .name("§f" + category.getDisplayName())
                .lore("§7Escolha a infração abaixo", "§8Escalonamento automático por regra"));

        if (rules.isEmpty()) {
            set(22, MenuItem.of(org.bukkit.Material.BARRIER)
                    .name("§cNenhuma regra configurada")
                    .lore("§7Edite punishments.yml"));
        } else {
            int idx = 0;
            for (PunishmentRules.Rule rule : rules) {
                if (idx >= SLOTS.length) break;
                int prior = data != null ? data.countByRuleId(target.getId(), rule.id()) : 0;
                int offense = prior + 1;
                String time = rule.resolveTime(PunishmentRules.offenseIndex(prior));

                List<String> lore = new ArrayList<>();
                lore.add("§7Infração: §f#" + offense);
                lore.add("§7Tempo sugerido: §f" + PunishmentRules.formatTime(time));
                lore.add("");
                lore.add("§7Escalonamento:");
                for (int i = 0; i < rule.steps().size(); i++) {
                    String marker = i == PunishmentRules.offenseIndex(prior) ? "§a▸ " : "§8  ";
                    lore.add(marker + "§7" + (i + 1) + "ª: §f" + PunishmentRules.formatTime(rule.steps().get(i)));
                }
                lore.add("§eClique esquerdo: confirmar");
                lore.add("§7Clique direito: alterar tempo");

                int slot = SLOTS[idx++];
                set(slot, MenuItem.of(rule.icon())
                        .name(rule.coloredLabel())
                        .lore(lore.toArray(String[]::new))
                        .click(p -> selectRule(p, rule, time, offense))
                        .rightClick(p -> {
                            clickSound();
                            PunishContext ctx = PunishContext.of(p.getUniqueId());
                            if (ctx == null) return;
                            prepareRule(ctx, rule, time, offense);
                            new PunishTimeMenu(p, target, this, rule).open();
                        }));
            }
        }

        back(45);
        closeButton(49);
    }

    private void selectRule(Player p, PunishmentRules.Rule rule, String time, int offense) {
        clickSound();
        PunishContext ctx = PunishContext.of(p.getUniqueId());
        if (ctx == null) return;
        prepareRule(ctx, rule, time, offense);
        new PunishConfirmMenu(p, target, this).open();
    }

    private static void prepareRule(PunishContext ctx, PunishmentRules.Rule rule,
                                    String time, int offense) {
        ctx.setKickMode(false);
        ctx.setCategory(rule.category());
        ctx.setReason(rule.reason());
        ctx.setRuleId(rule.id());
        ctx.setCause(rule.label());
        ctx.setTime(time);
        ctx.setOffenseNumber(offense);
    }
}
