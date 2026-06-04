package com.minecraft.game.bukkit.menu.clan;

import com.minecraft.game.Constant;
import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.clan.Clan;
import com.minecraft.game.api.clan.ClanMember;
import com.minecraft.game.api.clan.ClanRole;
import com.minecraft.game.api.clan.ClanTagFormatter;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Menu principal do sistema de clãs.
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
        set(22, MenuItem.of(Material.WHITE_BANNER)
                .name("§e§lCriar um Clã")
                .lore(
                        "§7Monte seu time e domine",
                        "§7a semi-anarquia com estilo!",
                        "",
                        "§7Nome: §f" + Constant.CLAN_NAME_MIN + "-" + Constant.CLAN_NAME_MAX + " caracteres",
                        "§7Tag: §f" + Constant.CLAN_TAG_MIN + "-" + Constant.CLAN_TAG_MAX + " caracteres",
                        "",
                        "§eClique para ver instruções"
                )
                .click(p -> {
                    clickSound();
                    account.send("§6§lCriar Clã",
                            "§7Use: §e/clan criar <nome> <tag>",
                            "§7Exemplo: §f/clan criar Savage SVG",
                            "",
                            "§7A tag aparece ao lado do seu nick",
                            "§7quando ativada em §e/prefs toggle clantag");
                }));

        set(40, MenuItem.of(Material.SPYGLASS)
                .name("§b§lExplorar Clãs")
                .lore("§7Veja os clãs com mais poder", "", "§eClique para listar")
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
                        "§7Membros: §f" + clan.getMembers().size() + "/" + Constant.CLAN_MAX_MEMBERS,
                        "§7Poder: §f" + clan.getPower(),
                        "§7Criado em: §f" + date
                ));

        set(20, MenuItem.of(Material.PLAYER_HEAD)
                .name("§a§lMembros")
                .lore("§7Gerencie o time", "", "§eClique para abrir")
                .click(p -> new ClanMembersMenu(p, account, clan, this).open()));

        set(22, MenuItem.of(Material.PAINTING)
                .name("§d§lCor da Tag")
                .lore(
                        "§7Atual: " + formattedTag,
                        "",
                        "§7Personalize a clantag",
                        "§7no tab e no chat!",
                        "",
                        "§eClique para escolher"
                )
                .click(p -> new ClanColorMenu(p, account, clan, this).open()));

        set(24, MenuItem.of(Material.WRITABLE_BOOK)
                .name("§e§lChat do Clã")
                .lore("§7Fale apenas com o time", "", "§f/cc <mensagem>"));

        ClanMember self = clan.getMember(account.getId());
        boolean canInvite = self != null && self.hasRole(ClanRole.RECRUITER);

        if (canInvite) {
            set(30, MenuItem.of(Material.EMERALD)
                    .name("§2§lConvidar")
                    .lore("§7Convide jogadores online", "", "§f/clan convidar <nick>")
                    .click(p -> {
                        clickSound();
                        account.send("§7Use §e/clan convidar <jogador>");
                    }));
        }

        if (clan.isLeader(account.getId())) {
            set(32, MenuItem.of(Material.TNT)
                    .name("§c§lDesfazer Clã")
                    .lore("§cAção irreversível!", "", "§f/clan deletar confirmar")
                    .click(p -> {
                        clickSound();
                        account.send("§cConfirme com §f/clan deletar confirmar");
                    }));
        } else {
            set(32, MenuItem.of(Material.OAK_DOOR)
                    .name("§c§lSair do Clã")
                    .lore("§7Deixar o time atual", "", "§f/clan sair")
                    .click(p -> {
                        clickSound();
                        account.send("§7Use §e/clan sair §7para sair do cla.");
                    }));
        }

        set(40, MenuItem.of(Material.SPYGLASS)
                .name("§b§lRanking")
                .lore("§7Top clãs por poder")
                .click(p -> new ClanTopMenu(p, account, this).open()));

        closeButton(44);
    }
}
