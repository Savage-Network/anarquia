package com.minecraft.core.bukkit.menu.server.collectible.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.controller.list.CollectibleController;
import com.minecraft.core.util.Util;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;


public class HatTypeSelectorMenu extends Menu {

    private final Account account;
    private final CollectibleController controller = Core.getCollectibleController();

    public HatTypeSelectorMenu(Player player, Menu last) {
        super(player, "Tipos de chapéus", last, 5);
        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        // Chapéus
        java.util.List<String> hatsLore = new java.util.ArrayList<>();
        hatsLore.add("§7Escolha um dos chapéus disponíveis");
        hatsLore.add("§7para usar nos lobbies.");
        hatsLore.add("");
        hatsLore.add("§fSelecionado:");

        java.util.List<CollectibleOperator> activeHats = CollectibleOperator.list(getPlayer()).stream()
                .filter(op -> op.getCategory() == CollectibleCategory.HAT)
                .collect(java.util.stream.Collectors.toList());
        if (activeHats.isEmpty()) {
            hatsLore.add("§7• Nenhum(a)");
        } else {
            for (CollectibleOperator op : activeHats) {
                String name = op.getCollectible() != null ? op.getCollectible().getName() : op.getCategory().getName();
                hatsLore.add("§a• " + name);
            }
        }
        int hatsUnlocked = account.getCollectibles(CollectibleCategory.HAT).size();
        int hatsTotal = controller.list(CollectibleCategory.HAT).size();
        int hatsPercent = Util.percentage(hatsUnlocked, hatsTotal);
        hatsLore.add("");
        hatsLore.add("§fDesbloqueados: §c" + hatsUnlocked + "/§c" + hatsTotal + " §8(" + hatsPercent + "%)");
        hatsLore.add("");
        hatsLore.add("§eClique para navegar,");

        addItem(11, Item.of(Material.LEATHER_HELMET)
                .name("§aChapéus")
                .lore(hatsLore)
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new CollectibleSelectorMenu(getPlayer(), this).handle();
                }));

        // Banners
        java.util.List<String> bannersLore = new java.util.ArrayList<>();
        bannersLore.add("§7Explore banners para usar como chapéu");
        bannersLore.add("§7e personalizar seu visual.");
        bannersLore.add("");
        bannersLore.add("§fSelecionado:");

        java.util.List<CollectibleOperator> activeBanners = CollectibleOperator.list(getPlayer()).stream()
                .filter(op -> op.getCategory() == CollectibleCategory.BANNER)
                .collect(java.util.stream.Collectors.toList());
        if (activeBanners.isEmpty()) {
            bannersLore.add("§7• Nenhum(a)");
        } else {
            for (CollectibleOperator op : activeBanners) {
                String name = op.getCollectible() != null ? op.getCollectible().getName() : op.getCategory().getName();
                bannersLore.add("§a• " + name);
            }
        }
        int bannersUnlocked = account.getCollectibles(CollectibleCategory.BANNER).size();
        int bannersTotal = controller.list(CollectibleCategory.BANNER).size();
        int bannersPercent = Util.percentage(bannersUnlocked, bannersTotal);
        bannersLore.add("");
        bannersLore.add("§fDesbloqueados: §c" + bannersUnlocked + "/§c" + bannersTotal + " §8(" + bannersPercent + "%)");
        bannersLore.add("");
        bannersLore.add("§eClique para navegar,");

        addItem(13, Item.of(Material.BANNER)
                .name("§aBanners")
                .lore(bannersLore)
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new CollectibleSelectorMenu(getPlayer(), CollectibleCategory.BANNER, this).handle();
                }));

        // Chapéus animados
        addItem(15, Item.of(Material.GOLD_AXE)
                .name("§aChapéus animados")
                .lore("§7Veja chapéus com animações especiais", "§7para um visual diferenciado.", "", "§eClique para navegar,")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new CollectibleSelectorMenu(getPlayer(), this).handle();
                }));

        addBackButton();
        addCloseButton();

        display();
    }
}