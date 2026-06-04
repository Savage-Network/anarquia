package com.minecraft.core.bukkit.menu.server.collectible;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.collectible.type.banner.BannerCollectible;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class CollectibleSelectorMenu extends Menu {

    private final Account account;
    private final CollectibleCategory category;

    private final List<Collectible> list;

    public CollectibleSelectorMenu(Player player, CollectibleCategory category, Menu last) {
        super(player, category.getName(), last, 6, 21);

        this.account = Core.getAccountController().of(player.getUniqueId());

        this.category = category;
        this.list = Core.getCollectibleController().list(category);
    }

    @Override
    public void handle() {
        clear();

        if (list.isEmpty())
            addItem(22, Item.of(Material.WEB, "§cNão há " + category.getName().toLowerCase() + "..."));
        else {
            list.sort(Comparator.comparingInt(collectible -> collectible.getRarity().ordinal()));

            buildPageItems(list, 10, (collectible, slot) -> {
                boolean using = account.isUsingCollectible(collectible), available = collectible.hasAccess(account.getRank().getType());

                List<String> lore = new ArrayList<>(Arrays.asList(
                        "§8" + collectible.getCategory().getName(),
                        ""
                ));

                if (!collectible.getLore().isEmpty()) {
                    lore.addAll(collectible.getLore());
                    lore.add("");
                }

                lore.addAll(Arrays.asList(
                        "§7Raridade: " + collectible.getRarity().getColoredName(),
                        "",
                        using ? "§cJá selecionada." : available ? "§eClique para selecionar!" : "§cIndisponível."
                ));

                Item icon = collectible.getCategory().equals(CollectibleCategory.BANNER) ? ((BannerCollectible) collectible).component()
                        : collectible.getIcon();

                Item item = (available ? icon : Item.of(Material.INK_SACK, 8))
                        .name((using ? "§e" : !available ? "§c" : "§a") + collectible.getName())
                        .lore(lore);

                // Head Style
                if (item.getType().equals(Material.SKULL_ITEM))
                    if (category == CollectibleCategory.EMOTION)
                        item.skullByUrl(collectible.getHeadValue());
                    else
                        item.skullByBase64(collectible.getHeadValue());

                addItem(slot, item.click(event -> {

                    if (!available) {
                        sound(MenuSound.ERROR);
                        account.send("§cVocê não possui " + collectible.getName() + ".");
                        return;
                    }

                    if (using) {
                        sound(MenuSound.ERROR);
                        account.send("§cO " + collectible.getName() + " já foi selecionado.");
                        return;
                    }

                    close();
                    sound(MenuSound.SUCCESS);

                    CollectibleOperator.unloadConflicts(getPlayer(), category);

                    CollectibleOperator operator = CollectibleOperator.of(getPlayer(), category);

                    if (operator != null) {
                        operator.cancel();
                        operator.handle(collectible);
                    } else
                        CollectibleOperator.start(getPlayer(), collectible);

                    account.send("§eO §6" + collectible.getName() + "§e foi selecionado.");
                }));
            });

            // Se tiver operador com essa categoria, criar item de remoção
            // -5 se tiver mais de uma página, e -6 se tiver apenas uma página
            CollectibleOperator operator = CollectibleOperator.of(getPlayer(), category);

            if (operator != null) {
                addItem(getTotalSlots() - (getTotalPages() > 1 ? 5 : 6), Item.of(Material.BARRIER, "§cRemover")
                        .click(event -> {
                            close();
                            sound(MenuSound.PAGINATED);

                            account.send("§eO §6" + operator.getCollectible().getName() + "§e foi removido.");

                            CollectibleOperator.unload(getPlayer(), category);
                        }));
            }
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
