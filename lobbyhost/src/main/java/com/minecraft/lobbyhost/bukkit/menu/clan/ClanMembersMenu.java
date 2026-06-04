package com.minecraft.lobbyhost.bukkit.menu.clan;

import com.minecraft.lobbyhost.account.Account;
import com.minecraft.lobbyhost.api.clan.Clan;
import com.minecraft.lobbyhost.api.clan.ClanMember;
import com.minecraft.lobbyhost.api.clan.ClanRole;
import com.minecraft.lobbyhost.bukkit.menu.Menu;
import com.minecraft.lobbyhost.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ClanMembersMenu extends Menu {

    private final Account account;
    private final Clan clan;

    public ClanMembersMenu(Player player, Account account, Clan clan, Menu parent) {
        super(player, com.minecraft.lobbyhost.util.ModernText.menuTitle("Membros do Clã"), 6, parent);
        this.account = account;
        this.clan = clan;
    }

    @Override
    public void build() {
        clear();

        List<ClanMember> sorted = new ArrayList<>(clan.getMembers().values());
        sorted.sort(Comparator.comparing((ClanMember m) -> m.getRole().ordinal()).reversed()
                .thenComparing(ClanMember::getName));

        int slot = 10;
        for (ClanMember member : sorted) {
            if (slot > 34) break;
            if (slot % 9 == 0 || slot % 9 == 8) slot += 2;

            boolean isSelf = member.getId().equals(account.getId());
            boolean canManage = clan.isLeader(account.getId()) && !member.getId().equals(clan.getLeader());

            MenuItem item = MenuItem.of(Material.PLAYER_HEAD)
                    .skullOwner(member.getName())
                    .name(member.getRole().getColor() + member.getRole().getSymbol() + " §f" + member.getName()
                            + (isSelf ? " §7(você)" : ""))
                    .lore(
                            "§7Cargo: " + member.getRole().getColor() + member.getRole().getDisplayName(),
                            "§7Entrou: §f" + formatDate(member.getJoinedAt()),
                            canManage ? "" : null,
                            canManage ? "§eClique para promover/rebaixar" : null
                    );

            if (canManage) {
                item.click(p -> cycleRole(member));
            }

            set(slot++, item);
        }

        back(49);
        closeButton(53);
    }

    private void cycleRole(ClanMember member) {
        if (!clan.isLeader(account.getId())) return;
        ClanRole next = switch (member.getRole()) {
            case MEMBER -> ClanRole.RECRUITER;
            case RECRUITER -> ClanRole.MEMBER;
            case LEADER -> ClanRole.LEADER;
        };
        if (member.getRole() == ClanRole.LEADER) return;

        Account target = com.minecraft.lobbyhost.Core.getAccountController().of(member.getId());
        if (target != null) {
            clan.promote(account, target, next);
        } else {
            member.setRole(next);
            clan.persist();
        }
        successSound();
        reopen();
    }

    private static String formatDate(long ms) {
        return new java.text.SimpleDateFormat("dd/MM/yy")
                .format(new java.util.Date(ms));
    }
}
