package com.minecraft.game.bukkit.shop.gui;

import com.minecraft.game.account.Account;
import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.bukkit.shop.ShopEconomy;
import com.minecraft.game.bukkit.shop.SignShopManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class ShopHubMenu extends Menu {

    private final Account account;

    public ShopHubMenu(Player player, Account account) {
        this(player, account, null);
    }

    public ShopHubMenu(Player player, Account account, Menu parent) {
        super(player, "§8§lSavageMC §7| §fMercado", 5, parent);
        this.account = account;
    }

    @Override
    public void build() {
        clear();
        String sym = ShopEconomy.symbol();

        ShopMenuLayout.header(
                this, account, Material.NETHER_STAR,
                "§6§lSavageMC §7| §fMercado",
                "§8▪ §7savagemc.net",
                "",
                "§7Escolha uma opcao abaixo"
        );

        set(20, MenuItem.of(Material.EMERALD)
                .name("§a§lLoja do Servidor")
                .lore(
                        "§7Compre blocos, comida, PvP",
                        "§7e ferramentas no spawn",
                        "",
                        "§eClique para abrir"
                )
                .glow()
                .click(p -> new ServerShopMenu(p, account, this).open()));

        set(22, MenuItem.of(Material.GOLD_INGOT)
                .name("§6§lVender Itens")
                .lore(
                        "§7Venda farm, minerios e drops",
                        "§7direto ao servidor",
                        "",
                        "§eClique para abrir"
                )
                .glow()
                .click(p -> new SellMenu(p, account, this).open()));

        set(24, MenuItem.of(Material.ENDER_CHEST)
                .name("§5§lLeilao")
                .lore(
                        "§7Compre e venda entre jogadores",
                        "§7Comando: §f/ah",
                        "",
                        "§eClique para abrir"
                )
                .click(p -> new AuctionBrowseMenu(p, account, this, 0).open()));

        set(30, MenuItem.of(Material.OAK_SIGN)
                .name("§e§lLojas de Placa")
                .lore(
                        "§7Monte sua loja com placas",
                        "§7Comando: §f/loja placa",
                        "",
                        "§eClique para tutorial"
                )
                .click(SignShopManager::sendHelp));

        set(32, MenuItem.of(Material.PLAYER_HEAD)
                .name("§b§lTroca Segura")
                .lore(
                        "§7Troque itens com seguranca",
                        "§7Comando: §f/trade <jogador>",
                        "",
                        "§eUse o comando no chat"
                )
                .click(p -> p.sendMessage("§7Use §f/trade <jogador> §7para convidar.")));

        fillIfEmpty(3, MenuItem.of(Material.LIME_STAINED_GLASS_PANE).name(" "));
        fillIfEmpty(5, MenuItem.of(Material.LIME_STAINED_GLASS_PANE).name(" "));
        fillIfEmpty(21, MenuItem.of(Material.YELLOW_STAINED_GLASS_PANE).name(" "));
        fillIfEmpty(23, MenuItem.of(Material.YELLOW_STAINED_GLASS_PANE).name(" "));

        closeButton(40);
    }
}
