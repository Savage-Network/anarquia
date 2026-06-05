package com.minecraft.game.bukkit.menu.clan;

import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanRole;
import com.minecraft.game.api.clan.ClanService;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Configurações do clã (base, PvP amigável, MOTD). */
public final class ClanSettingsMenu extends Menu {

    private final Account account;
    private final Clan clan;

    public ClanSettingsMenu(Player player, Account account, Clan clan, Menu parent) {
        super(player, "§8§lClã §7| §fConfigurações", 4, parent);
        this.account = account;
        this.clan = clan;
    }

    @Override
    public void build() {
        clear();
        boolean leader = clan.isLeader(account.getId());
        boolean recruiter = account.hasClanRole(ClanRole.RECRUITER);

        set(4, MenuItem.of(Material.WRITABLE_BOOK)
                .name("§e§lMOTD")
                .lore(
                        "§7Mensagem do clã:",
                        "§f" + clan.getMotd(),
                        "",
                        leader ? "§e/clan motd <mensagem>" : "§8Apenas o líder edita"
                ));

        set(11, MenuItem.of(Material.COMPASS)
                .name("§b§lBase do clã")
                .lore(
                        clan.hasHome()
                                ? "§7Base em §f" + clan.getStorage().getHomeWorld()
                                : "§7Nenhuma base definida",
                        "",
                        "§eClique §7- ir para base",
                        recruiter ? "§e/clan sethome §7- definir aqui" : "",
                        leader ? "§e/clan delhome §7- remover" : ""
                )
                .click(p -> {
                    clickSound();
                    ClanService.Result r = ClanService.teleportHome(p, account);
                    account.send(r.success() ? "§a" + r.message() : "§c" + r.message());
                    if (r.success()) successSound();
                }));

        if (leader) {
            set(15, MenuItem.of(Material.IRON_SWORD)
                    .name("§c§lPvP entre membros")
                    .lore(
                            "§7Estado: " + (clan.isFriendlyFire() ? "§aAtivado" : "§cDesativado"),
                            "",
                            "§eClique para alternar"
                    )
                    .click(p -> {
                        clickSound();
                        ClanService.Result r = ClanService.toggleFriendlyFire(account);
                        account.send(r.success() ? "§a" + r.message() : "§c" + r.message());
                        reopen();
                    }));
        }

        back(27);
        closeButton(31);
    }
}
