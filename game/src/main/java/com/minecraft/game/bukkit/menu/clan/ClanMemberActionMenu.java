package com.minecraft.game.bukkit.menu.clan;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanMember;
import com.minecraft.game.api.clan.ClanRole;
import com.minecraft.game.api.clan.ClanService;
import com.minecraft.game.api.clan.ClanTagFormatter;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Gerenciamento de um membro (promover, rebaixar, expulsar, transferir). */
public final class ClanMemberActionMenu extends Menu {

    private final Account account;
    private final Clan clan;
    private final ClanMember member;

    public ClanMemberActionMenu(Player player, Account account, Clan clan, ClanMember member, Menu parent) {
        super(player, "§8§l" + member.getName(), 3, parent);
        this.account = account;
        this.clan = clan;
        this.member = member;
    }

    @Override
    public void build() {
        clear();
        boolean isSelf = member.getId().equals(account.getId());
        boolean isLeader = clan.isLeader(account.getId());
        boolean targetIsLeader = clan.isLeader(member.getId());
        boolean canKick = !isSelf && !targetIsLeader
                && (isLeader || (account.hasClanRole(ClanRole.RECRUITER) && member.getRole() == ClanRole.MEMBER));

        set(4, MenuItem.of(Material.PLAYER_HEAD)
                .skullOwner(member.getName())
                .name(member.getRole().getColor() + member.getRole().getSymbol() + " §f" + member.getName())
                .lore(
                        "§7Cargo: " + member.getRole().getColor() + member.getRole().getDisplayName(),
                        "§7Clã: §f" + clan.getName(),
                        "§7Tag: " + ClanTagFormatter.formatBracketed(clan)
                ));

        Account target = Core.getAccountController().of(member.getId());

        if (isLeader && !targetIsLeader && target != null) {
            set(11, MenuItem.of(Material.LIME_DYE)
                    .name("§a§lPromover")
                    .lore("§7Tornar §aRecrutador", "", "§eClique para promover")
                    .click(p -> handleResult(ClanService.promote(account, target))));

            set(13, MenuItem.of(Material.ORANGE_DYE)
                    .name("§6§lRebaixar")
                    .lore("§7Tornar §7Membro", "", "§eClique para rebaixar")
                    .click(p -> handleResult(ClanService.demote(account, target))));

            set(15, MenuItem.of(Material.NETHER_STAR)
                    .name("§c§lTransferir liderança")
                    .lore("§7Passar o clã para este membro", "", "§cClique para confirmar")
                    .click(p -> handleResult(ClanService.transferLeadership(account, target))));
        }

        if (canKick && target != null) {
            set(22, MenuItem.of(Material.IRON_DOOR)
                    .name("§c§lExpulsar")
                    .lore("§7Remover do clã", "", "§cClique para expulsar")
                    .click(p -> handleResult(ClanService.kick(account, target))));
        }

        back(18);
        closeButton(26);
    }

    private void handleResult(ClanService.Result result) {
        clickSound();
        account.send(result.success() ? "§a" + result.message() : "§c" + result.message());
        if (result.success()) {
            successSound();
            if (parent != null) parent.open();
            else player.closeInventory();
        }
    }
}
