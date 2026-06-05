package com.minecraft.game.bukkit.menu.anticheat;

import com.minecraft.game.anticheat.AnticheatCheck;
import com.minecraft.game.anticheat.AnticheatConfig;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class AnticheatCategoryMenu extends Menu {

    private final AnticheatCheck.Category category;

    public AnticheatCategoryMenu(Player player, AnticheatCheck.Category category, Menu parent) {
        super(player, "§8§lAC §7| " + category.getDisplay(), 6, parent);
        this.category = category;
    }

    @Override
    public void build() {
        clear();

        set(4, MenuItem.of(category.getIcon())
                .name(category.getDisplay() + " §8— §fChecks")
                .lore("§7Clique para ativar/desativar cada detecção"));

        List<AnticheatCheck> checks = new ArrayList<>();
        for (AnticheatCheck c : AnticheatCheck.values()) {
            if (c.getCategory() == category) checks.add(c);
        }

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        for (int i = 0; i < checks.size() && i < slots.length; i++) {
            AnticheatCheck check = checks.get(i);
            boolean on = AnticheatConfig.isCheckEnabled(check);
            MenuItem item = MenuItem.of(check.getIcon())
                    .name((on ? "§a● " : "§8● ") + "§f" + check.getDisplayName())
                    .lore(
                            "§7" + check.getDescription(),
                            "",
                            on ? "§aAtivado" : "§cDesativado",
                            "§eClique para alternar")
                    .click(p -> {
                        clickSound();
                        AnticheatConfig.toggleCheck(check);
                        AnticheatConfig.save();
                        reopen();
                    });
            if (on) item.glow();
            set(slots[i], item);
        }

        back(45);
        closeButton(49);
    }
}
