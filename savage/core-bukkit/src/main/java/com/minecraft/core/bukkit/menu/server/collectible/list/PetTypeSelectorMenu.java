package com.minecraft.core.bukkit.menu.server.collectible.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.controller.list.CollectibleController;
import com.minecraft.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PetTypeSelectorMenu extends Menu {

    private final Account account;
    private final CollectibleController controller = Core.getCollectibleController();

    public PetTypeSelectorMenu(Player player, Menu last) {
        super(player, "Companheiros", last, 5);
        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        addCategoryEntry(11, Material.BONE, "§aPets", CollectibleCategory.PET);
        addCategoryEntry(13, Material.LEASH, "§aBalões", CollectibleCategory.BALLOON);
        addCategoryEntry(15, Material.SKULL_ITEM, "§aCompanheiros", CollectibleCategory.COMPANION);
        addCategoryEntry(22, Material.FEATHER, "§aAsas", CollectibleCategory.WINGS);

        addBackButton();
        addCloseButton();
        display();
    }

    private void addCategoryEntry(int slot, Material material, String name, CollectibleCategory category) {
        List<String> lore = new ArrayList<>();
        lore.addAll(category.getLore());
        lore.add("");
        lore.add("§fSelecionado:");

        List<CollectibleOperator> active = CollectibleOperator.list(getPlayer()).stream()
                .filter(op -> op.getCategory() == category)
                .collect(Collectors.toList());

        if (active.isEmpty()) {
            lore.add("§7• Nenhum(a)");
        } else {
            for (CollectibleOperator op : active) {
                String label = op.getCollectible() != null ? op.getCollectible().getName() : category.getName();
                lore.add("§a• " + label);
            }
        }

        int unlocked = account.getCollectibles(category).size();
        int total = controller.list(category).size();
        lore.add("");
        lore.add("§fDesbloqueados: §c" + unlocked + "/§c" + total + " §8(" + Util.percentage(unlocked, total) + "%)");
        lore.add("");
        lore.add("§eClique para navegar!");

        addItem(slot, Item.of(material)
                .name(name)
                .lore(lore)
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new CollectibleSelectorMenu(getPlayer(), category, this).handle();
                }));
    }
}
