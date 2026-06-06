package com.minecraft.game.bukkit.menu.prefs;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.message.MenuTexts;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Menu de preferências por seção: Social, Visibilidade, Chat, Notificações, Anticheat e Staff.
 */
public final class PrefsMenu extends Menu {

    private final Account account;

    public PrefsMenu(Player player, Account account) {
        super(player, MenuTexts.legacy(player, "menus.prefs.title"), 6, null);
        this.account = account;
    }

    @Override
    public void build() {
        clear();

        set(4, MenuItem.of(Material.COMPARATOR)
                .name("§6§lSuas preferências")
                .lore(
                        "§7Personalize social, chat e staff",
                        "",
                        "§7Brilho = ativado · sem brilho = desativado",
                        "",
                        "§eClique para alternar"
                ));

        int slotIdx = 0;
        int[] slots = PrefsToggle.itemSlots();

        for (PrefsToggle.Category category : PrefsToggle.layoutOrder()) {
            if (!hasVisibleInCategory(category)) continue;
            if (slotIdx >= slots.length) break;

            set(slots[slotIdx++], categoryHeader(category));

            for (PrefsToggle toggle : PrefsToggle.byCategory(category)) {
                if (!PrefsToggle.visible(toggle, account)) continue;
                if (slotIdx >= slots.length) break;
                set(slots[slotIdx++], toggleItem(toggle));
            }
        }

        closeButton(49);
    }

    private boolean hasVisibleInCategory(PrefsToggle.Category category) {
        for (PrefsToggle toggle : PrefsToggle.byCategory(category)) {
            if (PrefsToggle.visible(toggle, account)) return true;
        }
        return false;
    }

    private MenuItem categoryHeader(PrefsToggle.Category category) {
        return MenuItem.of(category.pane())
                .name(category.title())
                .lore(category.subtitle());
    }

    private MenuItem toggleItem(PrefsToggle toggle) {
        boolean on = toggle.get(account.getToggle());
        MenuItem item = MenuItem.of(toggle.icon())
                .name("§f" + toggle.label() + " " + (on ? "§a●" : "§8○"))
                .lore(
                        "§7Estado: " + (on ? "§aAtivado" : "§cDesativado"),
                        "",
                        "§eClique para alternar"
                )
                .click(p -> {
                    clickSound();
                    toggle.flip(account);
                    reopen();
                });
        if (on) item.glow();
        return item;
    }
}
