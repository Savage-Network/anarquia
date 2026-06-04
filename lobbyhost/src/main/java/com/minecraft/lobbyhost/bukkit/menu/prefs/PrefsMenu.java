package com.minecraft.lobbyhost.bukkit.menu.prefs;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import com.minecraft.lobbyhost.util.ModernText;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Menu de preferências por seção: Social, Visibilidade, Chat, Anticheat e Staff.
 */
public final class PrefsMenu extends Menu {

    private static final PrefsToggle.Category[] LAYOUT_ORDER = {
            PrefsToggle.Category.SOCIAL,
            PrefsToggle.Category.VISIBILITY,
            PrefsToggle.Category.CHAT,
            PrefsToggle.Category.ANTICHEAT,
            PrefsToggle.Category.STAFF
    };

    private static final int[][] SECTION_SLOTS = {
            {10, 11},
            {19, 20},
            {28, 29},
            {31, 32},
            {37, 38}
    };

    private final Account account;

    public PrefsMenu(Player player, Account account) {
        super(player, ModernText.menuTitle("Preferências"), 6, null);
        this.account = account;
    }

    @Override
    public void build() {
        clear();

        set(4, MenuItem.of(Material.COMPARATOR)
                .miniName(ModernText.brandGradient("Suas preferências"))
                .miniLore(
                        ModernText.mini("Personalize social, chat e staff"),
                        "",
                        ModernText.mini("Brilho = ativado · sem brilho = desativado"),
                        "",
                        ModernText.action("Clique para alternar")
                ));

        int sectionIndex = 0;
        for (PrefsToggle.Category category : LAYOUT_ORDER) {
            if (!hasVisibleInCategory(category)) continue;
            if (sectionIndex >= SECTION_SLOTS.length) break;

            int headerSlot = SECTION_SLOTS[sectionIndex][0];
            int toggleSlot = SECTION_SLOTS[sectionIndex][1];
            sectionIndex++;

            set(headerSlot, categoryHeader(category));

            for (PrefsToggle toggle : PrefsToggle.byCategory(category)) {
                if (!PrefsToggle.visible(toggle, account)) continue;
                if (toggleSlot > headerSlot + 6) break;
                set(toggleSlot++, toggleItem(toggle));
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
                .miniName(category.title())
                .miniLore(category.subtitle());
    }

    private MenuItem toggleItem(PrefsToggle toggle) {
        boolean on = toggle.get(account.getToggle());
        MenuItem item = MenuItem.of(toggle.icon())
                .miniName(ModernText.label(toggle.label()) + " " + ModernText.dot(on))
                .miniLore(
                        "<!italic>Estado: " + ModernText.status(on),
                        "",
                        ModernText.action("Clique para alternar")
                )
                .click(p -> {
                    toggle.flip(account);
                    reopen();
                });
        if (on) item.glow();
        return item;
    }
}
