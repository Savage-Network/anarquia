package com.minecraft.core.bukkit.menu.server.collectible;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.server.collectible.list.CollectibleSelectorMenu;
import com.minecraft.core.bukkit.menu.server.collectible.list.ImageAuraSelectorMenu;
import com.minecraft.core.bukkit.menu.server.collectible.list.HatTypeSelectorMenu;
import com.minecraft.core.bukkit.menu.server.collectible.list.PetTypeSelectorMenu;
import com.minecraft.core.bukkit.menu.server.collectible.list.TitleCollectibleSelectorMenu;
import com.minecraft.core.controller.list.CollectibleController;
import com.minecraft.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CollectibleGroupMenu extends Menu {

    private final Account account;
    private final String group;
    private final CollectibleController controller = Core.getCollectibleController();

    public CollectibleGroupMenu(Player player, String group, Menu last) {
        super(player, "Cosméticos — " + group, last, 6);
        this.account = Core.getAccountController().of(player.getUniqueId());
        this.group = group;
    }

    @Override
    public void handle() {
        clear();

        List<CollectibleCategory> categories = CollectibleCategory.byGroup(group);
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

        for (int i = 0; i < categories.size() && i < slots.length; i++) {
            CollectibleCategory category = categories.get(i);
            addCategoryItem(slots[i], categoryIcon(category), category);
        }

        addBackButton(49);
        addCloseButton(53);
        display();
    }

    private Material categoryIcon(CollectibleCategory category) {
        switch (category) {
            case HAT:
                return Material.GOLD_HELMET;
            case EMOTION:
                return Material.SKULL_ITEM;
            case BANNER:
                return Material.BANNER;
            case CAPES:
                return Material.NETHER_STAR;
            case CLOTHING:
                return Material.LEATHER_CHESTPLATE;
            case GADGET:
                return Material.BLAZE_ROD;
            case PARTICLE:
                return Material.BLAZE_POWDER;
            case AURA:
                return Material.BEACON;
            case TRAIL:
                return Material.REDSTONE;
            case MUTATION:
                return Material.POTION;
            case KILL_EFFECT:
                return Material.IRON_SWORD;
            case FOOTPRINT:
                return Material.SOUL_SAND;
            case JUMP_EFFECT:
                return Material.FEATHER;
            case DEATH_EFFECT:
                return Material.SKULL_ITEM;
            case BALLOON:
                return Material.LEASH;
            case PET:
                return Material.BONE;
            case COMPANION:
                return Material.SKULL_ITEM;
            case WINGS:
                return Material.FEATHER;
            case ARTIFACT:
                return Material.COMMAND;
            case TITLE:
                return Material.NAME_TAG;
            case JOIN_MESSAGE:
                return Material.BOOK;
            default:
                return Material.NETHER_STAR;
        }
    }

    private void addCategoryItem(int slot, Material material, CollectibleCategory category) {
        List<String> lore = new ArrayList<>(category.getLore());
        lore.add("");
        lore.add("§fSelecionado:");

        List<CollectibleOperator> active = CollectibleOperator.list(getPlayer()).stream()
                .filter(op -> op.getCategory() == category)
                .collect(java.util.stream.Collectors.toList());

        if (active.isEmpty()) {
            lore.add("§7• Nenhum(a)");
        } else {
            for (CollectibleOperator op : active) {
                String name = op.getCollectible() != null ? op.getCollectible().getName() : category.getName();
                lore.add("§a• " + name);
            }
        }

        int unlocked = account.getCollectibles(category).size();
        int total = controller.list(category).size();
        lore.add("");
        lore.add("§fDesbloqueados: §c" + unlocked + "/§c" + total + " §8(" + Util.percentage(unlocked, total) + "%)");
        lore.add("");
        lore.add("§eClique para navegar!");

        addItem(slot, Item.of(material)
                .name("§a" + category.getName())
                .lore(lore)
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    openCategory(category);
                }));
    }

    private void openCategory(CollectibleCategory category) {
        switch (category) {
            case TITLE:
                new TitleCollectibleSelectorMenu(getPlayer(), this).handle();
                break;
            case HAT:
            case BANNER:
                new HatTypeSelectorMenu(getPlayer(), this).handle();
                break;
            case BALLOON:
            case PET:
            case COMPANION:
            case WINGS:
                new PetTypeSelectorMenu(getPlayer(), this).handle();
                break;
            case AURA:
                new ImageAuraSelectorMenu(getPlayer(), this).handle();
                break;
            default:
                new CollectibleSelectorMenu(getPlayer(), category, this).handle();
                break;
        }
    }
}
