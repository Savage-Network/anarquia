package com.minecraft.lobbyhost.bukkit.menu.clan;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.clan.Clan;
import com.minecraft.lobbyhost.api.clan.ClanColor;
import com.minecraft.lobbyhost.api.clan.ClanMember;
import com.minecraft.lobbyhost.api.clan.ClanRole;
import com.minecraft.lobbyhost.api.clan.ClanSync;
import com.minecraft.lobbyhost.api.clan.ClanTagFormatter;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import org.bukkit.entity.Player;

public final class ClanColorMenu extends Menu {

    private final Account account;
    private final Clan clan;

    public ClanColorMenu(Player player, Account account, Clan clan, Menu parent) {
        super(player, com.minecraft.lobbyhost.util.ModernText.menuTitle("Cores da Tag"), 5, parent);
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

        int slot = 19;
        for (ClanColor color : ClanColor.values()) {
            if (slot > 25) break;
            boolean access = color.isFree()
                    || (color.getPermission() != null && account.hasPermission(color.getPermission()))
                    || account.isStaffer();

            String preview = switch (color) {
                case BRASIL -> "§e[§eBR§aAS§aIL§e]";
                case YINGYANG -> "§f[§fYIN§0YANG§f]";
                default -> color.getCode() + "[" + clan.getTagWithoutColors().toUpperCase() + "]";
            };

            set(slot++, MenuItem.of(color.getGlass())
                    .name((access ? "§a" : "§c") + color.getName())
                    .lore(
                            "§7Preview: " + preview,
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
