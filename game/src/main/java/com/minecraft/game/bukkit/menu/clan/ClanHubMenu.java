package com.minecraft.game.bukkit.menu.clan;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanMember;
import com.minecraft.game.api.clan.ClanRole;
import com.minecraft.game.api.clan.ClanTagFormatter;
import com.minecraft.game.backend.data.ClanData;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Menu principal do sistema de clãs (estilo ClanForge / SimpleClans+).
 */
public final class ClanHubMenu extends Menu {

    private final Account account;

    public ClanHubMenu(Player player, Account account) {
        super(player, "§8§lClãs §7| §fPainel", 5, null);
        this.account = account;
    }

    @Override
    public void build() {
        clear();

        if (!account.hasClan()) {
            buildNoClan();
            return;
        }
        buildWithClan(account.getClan());
    }

    private void buildNoClan() {
        ClanData data = Core.getClanData();
        int invites = data != null ? data.getPendingInvites(account.getId()).size() : 0;

        set(20, MenuItem.of(Material.WHITE_BANNER)
                .name("§e§lCriar Clã")
                .lore(
                        "§7Monte seu time no SavageMC",
                        "",
                        "§7Nome: §f" + Constant.CLAN_NAME_MIN + "-" + Constant.CLAN_NAME_MAX + " chars",
                        "§7Tag: §f" + Constant.CLAN_TAG_MIN + "-" + Constant.CLAN_TAG_MAX + " chars",
                        "",
                        "§e/clan criar <nome> <tag>"
                )
                .click(p -> {
                    clickSound();
                    account.send("§6§lCriar Clã", "§7Use: §e/clan criar <nome> <tag>",
                            "§7Exemplo: §f/clan criar Savage SVG");
                }));

        MenuItem inviteItem = MenuItem.of(Material.PAPER)
                .name("§a§lConvites" + (invites > 0 ? " §8(§f" + invites + "§8)" : ""))
                .lore(
                        invites > 0 ? "§7Você tem convites pendentes!" : "§7Nenhum convite pendente",
                        "",
                        "§eClique para gerenciar"
                )
                .click(p -> new ClanInvitesMenu(p, account, this).open());
        if (invites > 0) inviteItem.glow();
        set(22, inviteItem);

        set(24, MenuItem.of(Material.BOOK)
                .name("§b§lExplorar Clãs")
                .lore("§7Lista completa de clãs", "", "§eClique para abrir")
                .click(p -> new ClanListMenu(p, account, this, 0).open()));

        set(40, MenuItem.of(Material.GOLD_INGOT)
                .name("§6§lRanking")
                .lore("§7Top clãs por poder")
                .click(p -> new ClanTopMenu(p, account, this).open()));

        closeButton(44);
    }

    private void buildWithClan(Clan clan) {
        String formattedTag = ClanTagFormatter.formatBracketed(clan);
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
                .format(new Date(clan.getCreatedAt()));

        set(4, MenuItem.of(Material.NETHER_STAR).glow()
                .name("§6§l" + clan.getName())
                .lore(
                        "§7Tag: " + formattedTag,
                        "§7MOTD: §f" + clan.getMotd(),
                        "§7Membros: §f" + clan.getMembers().size() + "/" + Constant.CLAN_MAX_MEMBERS,
                        "§7Online: §a" + clan.getOnlineCount(),
                        "§7Poder: §f" + clan.getPower(),
                        "§7Criado: §f" + date
                )
                .click(p -> new ClanViewMenu(p, account, clan, this).open()));

        set(19, MenuItem.of(Material.PLAYER_HEAD)
                .name("§a§lMembros")
                .lore("§7Gerencie cargos e expulsões", "", "§eClique para abrir")
                .click(p -> new ClanMembersMenu(p, account, clan, this).open()));

        set(21, MenuItem.of(Material.PAINTING)
                .name("§d§lCor da Tag")
                .lore("§7Atual: " + formattedTag, "", "§eClique para personalizar")
                .click(p -> new ClanColorMenu(p, account, clan, this).open()));

        set(23, MenuItem.of(Material.WRITABLE_BOOK)
                .name("§e§lChat do Clã")
                .lore("§7Canal privado do time", "", "§f/cc <mensagem>"));

        set(25, MenuItem.of(Material.COMPARATOR)
                .name("§b§lConfigurações")
                .lore("§7Base, MOTD, PvP amigável", "", "§eClique para abrir")
                .click(p -> new ClanSettingsMenu(p, account, clan, this).open()));

        ClanMember self = clan.getMember(account.getId());
        if (self != null && self.hasRole(ClanRole.RECRUITER)) {
            set(30, MenuItem.of(Material.EMERALD)
                    .name("§2§lConvidar")
                    .lore("§7Convide jogadores online", "", "§eClique para abrir")
                    .click(p -> new ClanInviteMenu(p, account, clan, this).open()));
        }

        if (clan.isLeader(account.getId())) {
            set(32, MenuItem.of(Material.TNT)
                    .name("§c§lDesfazer Clã")
                    .lore("§cIrreversível!", "", "§f/clan deletar confirmar")
                    .click(p -> account.send("§cConfirme: §f/clan deletar confirmar")));
        } else {
            set(32, MenuItem.of(Material.OAK_DOOR)
                    .name("§c§lSair do Clã")
                    .lore("§7Deixar o time", "", "§f/clan sair")
                    .click(p -> account.send("§7Use §e/clan sair")));
        }

        set(40, MenuItem.of(Material.GOLD_INGOT)
                .name("§6§lRanking")
                .click(p -> new ClanTopMenu(p, account, this).open()));

        set(42, MenuItem.of(Material.BOOK)
                .name("§b§lLista de Clãs")
                .click(p -> new ClanListMenu(p, account, this, 0).open()));

        closeButton(44);
    }
}
