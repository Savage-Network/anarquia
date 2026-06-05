package com.minecraft.game.bukkit.menu.clan;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanRequest;
import com.minecraft.game.api.clan.ClanService;
import com.minecraft.game.api.clan.ClanTagFormatter;
import com.minecraft.game.backend.data.ClanData;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/** Convites pendentes — aceitar/recusar via GUI. */
public final class ClanInvitesMenu extends Menu {

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

    private final Account account;

    public ClanInvitesMenu(Player player, Account account, Menu parent) {
        super(player, "§8§lClãs §7| §fConvites", 5, parent);
        this.account = account;
    }

    @Override
    public void build() {
        clear();
        ClanData data = Core.getClanData();
        List<ClanRequest> invites = data != null ? data.getPendingInvites(account.getId()) : List.of();

        if (invites.isEmpty()) {
            set(22, MenuItem.of(Material.BARRIER)
                    .name("§7Nenhum convite")
                    .lore("§8Quando alguém convidar você,", "§8aparecerá aqui."));
        } else {
            int idx = 0;
            for (ClanRequest req : invites) {
                if (idx >= SLOTS.length) break;
                Clan clan = data.of(req.getClanId());
                if (clan == null) continue;

                Account inviter = Core.getAccountController().of(req.getAuthor());
                String inviterName = inviter != null ? inviter.getNickname() : "?";
                int slot = SLOTS[idx++];

                set(slot, MenuItem.of(Material.PAPER)
                        .name("§b" + clan.getName())
                        .lore(
                                "§7Tag: " + ClanTagFormatter.formatBracketed(clan),
                                "§7Convidado por: §f" + inviterName,
                                "§7Membros: §f" + clan.getMembers().size(),
                                "",
                                "§aClique esquerdo §7→ aceitar",
                                "§cClique direito §7→ recusar"
                        )
                        .click(p -> {
                            clickSound();
                            ClanService.Result r = ClanService.accept(account, clan.getId());
                            if (r.success()) {
                                successSound();
                                p.closeInventory();
                            }
                            account.send(r.success() ? "§a" + r.message() : "§c" + r.message());
                        }));
            }
        }

        if (parent != null) back(40);
        closeButton(44);
    }
}
