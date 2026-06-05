package com.minecraft.game.bukkit.menu.punish;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.punishment.PunishContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class PunishKickMenu extends Menu {

    private static final String[] REASONS = {
            "Comportamento inadequado",
            "Spam / Flood",
            "Provocação / Toxicidade",
            "Skin ou nick inapropriado",
            "Outro motivo"
    };

    private final Account target;

    public PunishKickMenu(Player player, Account target, Menu parent) {
        super(player, "§8§lKick §7| §f" + target.getName(), 4, parent);
        this.target = target;
    }

    @Override
    public void build() {
        clear();
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < REASONS.length && i < slots.length; i++) {
            String reason = REASONS[i];
            set(slots[i], MenuItem.of(Material.IRON_DOOR)
                    .name("§6" + reason)
                    .lore("§7Expulsar com este motivo", "", "§eClique para confirmar")
                    .click(p -> {
                        clickSound();
                        PunishContext ctx = PunishContext.of(p.getUniqueId());
                        if (ctx != null) {
                            ctx.setKickMode(true);
                            ctx.setCause(reason);
                        }
                        new PunishConfirmMenu(p, target, this).open();
                    }));
        }

        back(27);
        closeButton(31);
    }
}
