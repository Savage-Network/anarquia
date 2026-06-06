package com.minecraft.game.bukkit.menu.clan;

import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanColor;
import com.minecraft.game.api.clan.ClanMember;
import com.minecraft.game.api.clan.ClanRole;
import com.minecraft.game.api.clan.ClanSync;
import com.minecraft.game.api.clan.ClanTagFormatter;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.entity.Player;

public final class ClanColorMenu extends Menu {

    private static final int[] COLOR_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final Account account;
    private final Clan clan;

    public ClanColorMenu(Player player, Account account, Clan clan, Menu parent) {
        super(player, "§8§lClã §7| §fCores da Tag", 5, parent);
        this.account = account;
        this.clan = clan;
    }

    @Override
    public void build() {
        clear();

        ClanMember member = clan.getMember(account.getId());
        if (member == null || !member.hasRole(ClanRole.RECRUITER)) {
            set(22, MenuItem.of(org.bukkit.Material.BARRIER)
                    .name("§cSem permissão")
                    .lore("§7Apenas Recrutador ou Líder", "§7podem alterar a cor."));
            back(40);
            return;
        }

        set(4, MenuItem.of(org.bukkit.Material.NAME_TAG).glow()
                .name("§fPreview: " + ClanTagFormatter.formatBracketed(clan))
                .lore("§7Sua clantag no servidor"));

        String tagSample = clan.getTagWithoutColors();
        int slotIndex = 0;
        for (ClanColor color : ClanColor.values()) {
            if (slotIndex >= COLOR_SLOTS.length) break;

            boolean access = color.isFree()
                    || (color.getPermission() != null && account.hasPermission(color.getPermission()))
                    || account.isStaffer();

            String preview = ClanTagFormatter.formatPreview(color, tagSample);
            String loreGradient = color.getGradientMode() == ClanColor.GradientMode.HEX
                    ? "§7Gradiente §f" + color.getGradientFrom() + " §7→ §f" + color.getGradientTo()
                    : null;

            set(COLOR_SLOTS[slotIndex++], MenuItem.of(color.getGlass())
                    .name((access ? "§a" : "§c") + color.getName())
                    .lore(
                            "§7Preview: " + preview,
                            loreGradient,
                            access ? "§eClique para aplicar" : "§cBloqueado",
                            color.getPermission() != null && !access
                                    ? "§7Requer: §f" + color.getPermission() : null
                    )
                    .click(p -> {
                        if (!access) {
                            clickSound();
                            account.send("§cVoce nao tem acesso a esta cor.");
                            return;
                        }
                        clan.setTagColor(color.hasMixedTag() ? color.getId() : color.getCode());
                        clan.persist();
                        ClanSync.refreshMembers(clan);
                        successSound();
                        account.send("§aCor da tag alterada para " + color.getCode() + color.getName() + "§a.");
                        reopen();
                    }));
        }

        back(40);
        closeButton(44);
    }
}
