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
        super(player, "§8§lEconomia §7| §fCentral", 5, null);
        this.account = account;
    }

    @Override
    public void build() {
        clear();
        String sym = ShopEconomy.symbol();

        set(4, MenuItem.of(Material.NETHER_STAR)
                .name("§6§lSAVAGE §7| §fMercado")
                .lore("§7Saldo: §a" + sym + account.getGolds()));

        set(20, MenuItem.of(Material.EMERALD)
                .name("§a§lLoja do Servidor")
                .lore("§7Compre itens com GUI", "§7estilo BAWMC", "", "§eClique para abrir")
                .click(p -> new ServerShopMenu(p, account, this).open()));

        set(22, MenuItem.of(Material.GOLD_INGOT)
                .name("§6§lVender Itens")
                .lore("§7Venda para o servidor", "§7precos em sell-prices.yml", "", "§eClique para abrir")
                .click(p -> new SellMenu(p, account, this).open()));

        set(24, MenuItem.of(Material.ENDER_CHEST)
                .name("§5§lLeilao (/ah)")
                .lore("§7Compre e venda entre jogadores", "", "§eClique para abrir")
                .click(p -> new AuctionBrowseMenu(p, account, this, 0).open()));

        set(30, MenuItem.of(Material.OAK_SIGN)
                .name("§e§lLojas de Placa")
                .lore("§7Chest shop com placas", "", "§eClique para tutorial")
                .click(p -> SignShopManager.sendHelp(p)));

        set(32, MenuItem.of(Material.PLAYER_HEAD)
                .name("§b§lTroca Segura")
                .lore("§7/trade <jogador>", "", "§eUse o comando")
                .click(p -> p.sendMessage("§7Use §f/trade <jogador> §7para convidar.")));

        closeButton(40);
    }
}
