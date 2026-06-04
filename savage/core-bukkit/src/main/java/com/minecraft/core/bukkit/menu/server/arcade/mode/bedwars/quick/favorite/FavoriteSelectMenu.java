package com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.quick.favorite;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.core.member.list.bedwars.objects.menu.BedMenuMetadata;
import com.minecraft.core.member.list.bedwars.objects.menu.item.BedItem;
import com.minecraft.core.member.list.bedwars.objects.menu.type.BedMenuType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FavoriteSelectMenu extends Menu {

    private final BedMember member;

    private final int selectedSlot;

    public FavoriteSelectMenu(Player player, BedMember member, Menu last, int selectedSlot) {
        super(player, "Selecionando favorito...", last, 6, 21);

        this.member = member;
        this.selectedSlot = selectedSlot + 9;
    }

    @Override
    public void handle() {
        clear();

        BedMenuMetadata menu = member.getMenu(BedMenuType.FAVORITE);

        if (menu == null)
            addErrorButton("§cNão foi possível visualizar o inventário...");
        else {
            List<BedItem> yourItems = menu.getItemList();

            List<BedWarsItem> rootItems = BedWarsItem.list().stream()
                    .filter(root -> yourItems.stream().noneMatch(your -> your.getName().equalsIgnoreCase(root.getName())))
                    .collect(Collectors.toList());

            buildPageItems(rootItems, 10, (item, slot) -> {
                List<String> lore = new ArrayList<>(item.getLore());

                if (!lore.isEmpty())
                    lore.add("");

                lore.add("§eClique para favoritar!");

                addItem(slot, Item.fromStack(item.getStack())
                        .name("§a" + item.getName())
                        .flags(ItemFlag.values())
                        .lore(lore)
                        .click(event -> {
                            sound(MenuSound.DONE);

                            List<BedItem> itemList = menu.getItemList();

                            itemList.removeIf(root -> root.getSlot() == selectedSlot);
                            itemList.add(new BedItem(selectedSlot, item.getName()));

                            member.updateMenu(menu);

                            getLast().handle();
                        }));
            });
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
