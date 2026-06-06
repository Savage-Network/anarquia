package com.minecraft.game.bukkit.menu.kit;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.kit.KitDefinition;
import com.minecraft.game.bukkit.kit.KitManager;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.message.MenuTexts;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class KitHubMenu extends Menu {

    private final Account account;

    public KitHubMenu(Player player, Account account) {
        super(player, MenuTexts.title(player, "menus.kit.hub-title"), 5, null);
        this.account = account;
    }

    @Override
    public void build() {
        clear();

        set(4, MenuItem.of(Material.CHEST)
                .name(MenuTexts.legacy(player, "menus.kit.hub-savage"))
                .lore(
                        MenuTexts.legacy(player, "menus.kit.hub-desc"),
                        MenuTexts.legacy(player, "menus.kit.hub-cooldown-note"),
                        "",
                        MenuTexts.legacy(player, "menus.kit.hub-left"),
                        MenuTexts.legacy(player, "menus.kit.hub-right")
                ));

        List<KitDefinition> kits = new ArrayList<>(KitManager.all().values());
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32};
        for (int i = 0; i < kits.size() && i < slots.length; i++) {
            KitDefinition kit = kits.get(i);
            set(slots[i], buildKitEntry(kit));
        }

        closeButton(40);
    }

    private MenuItem buildKitEntry(KitDefinition kit) {
        boolean access = KitManager.canUse(account, kit);
        long cdLeft = account.getCooldownRemaining(kit.cooldownKey());
        boolean onCooldown = !account.isStaffer() && kit.getCooldownMs() > 0 && cdLeft > 0;

        List<String> lore = new ArrayList<>();
        lore.add(MenuTexts.format(player, "menus.kit.min-rank",
                MenuTexts.map("rank", kit.getMinRank().name())));
        if (kit.getCooldownMs() > 0) {
            lore.add(MenuTexts.format(player, "menus.kit.cooldown-label",
                    MenuTexts.map("time", formatCooldown(kit.getCooldownMs()))));
        }
        if (onCooldown) {
            lore.add(MenuTexts.format(player, "menus.kit.wait-seconds",
                    MenuTexts.map("seconds", String.valueOf(Math.max(1, cdLeft / 1000L)))));
        }
        lore.add(MenuTexts.format(player, "menus.kit.content-count",
                MenuTexts.map("count", String.valueOf(kit.contentCount()))));
        for (String line : kit.contentSummaryLines(3)) {
            lore.add("§8  " + line);
        }
        lore.add("");
        if (!access) {
            lore.add(MenuTexts.legacy(player, "menus.kit.no-access"));
        } else if (onCooldown) {
            lore.add(MenuTexts.legacy(player, "menus.kit.unavailable"));
        } else {
            lore.add(MenuTexts.legacy(player, "menus.kit.left-receive"));
        }
        lore.add(MenuTexts.legacy(player, "menus.kit.right-preview"));

        String status = access ? (onCooldown ? "§c⏳ " : "§a✔ ") : "§c✖ ";
        MenuItem entry = MenuItem.of(kit.iconMaterial())
                .name(status + kit.getDisplayName().replace('&', '§'))
                .lore(lore.toArray(new String[0]))
                .click(p -> {
                    clickSound();
                    String result = KitManager.apply(account, p, kit.getId());
                    p.sendMessage(result);
                    if (result.startsWith("§a")) {
                        successSound();
                        p.closeInventory();
                    } else {
                        reopen();
                    }
                })
                .rightClick(p -> {
                    clickSound();
                    new KitPreviewMenu(p, kit, this).open();
                });

        if (access && !onCooldown) {
            entry.glow();
        }
        return entry;
    }

    private static String formatCooldown(long cooldownMs) {
        long seconds = cooldownMs / 1000L;
        if (seconds >= 3600L && seconds % 3600L == 0) {
            return (seconds / 3600L) + "h";
        }
        if (seconds >= 60L && seconds % 60L == 0) {
            return (seconds / 60L) + "min";
        }
        return seconds + "s";
    }
}
