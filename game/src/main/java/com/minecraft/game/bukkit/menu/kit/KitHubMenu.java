package com.minecraft.game.bukkit.menu.kit;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.kit.KitDefinition;
import com.minecraft.game.bukkit.kit.KitManager;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class KitHubMenu extends Menu {

    private final Account account;

    public KitHubMenu(Player player, Account account) {
        super(player, "§8§lSavageMC §7| §fKits", 5, null);
        this.account = account;
    }

    @Override
    public void build() {
        clear();

        set(4, MenuItem.of(Material.CHEST)
                .name("§6§lKits SavageMC")
                .lore("§7Escolha um kit abaixo", "§7Cooldown por kit"));

        List<KitDefinition> kits = new ArrayList<>(KitManager.all().values());
        int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32};
        for (int i = 0; i < kits.size() && i < slots.length; i++) {
            KitDefinition kit = kits.get(i);
            boolean access = KitManager.canUse(account, kit);
            long cdLeft = account.getCooldownRemaining(kit.cooldownKey());
            boolean onCooldown = !account.isStaffer() && kit.getCooldownMs() > 0 && cdLeft > 0;

            List<String> lore = new ArrayList<>();
            lore.add("§7Rank: §f" + kit.getMinRank().name());
            if (kit.getCooldownMs() > 0) {
                lore.add("§7Cooldown: §f" + (kit.getCooldownMs() / 1000L) + "s");
            }
            if (onCooldown) {
                lore.add("§cAguarde §f" + Math.max(1, cdLeft / 1000L) + "s");
            }
            lore.add("");
            if (!access) {
                lore.add("§cSem acesso");
            } else if (onCooldown) {
                lore.add("§cIndisponivel");
            } else {
                lore.add("§eClique para receber");
            }

            Material icon = kit.iconMaterial();
            String status = access ? (onCooldown ? "§c⏳ " : "§a✔ ") : "§c✖ ";
            MenuItem entry = MenuItem.of(icon)
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
                    });
            if (access && !onCooldown) entry.glow();
            set(slots[i], entry);
        }

        closeButton(40);
    }
}
