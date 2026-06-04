package com.minecraft.core.bukkit.menu.server.arcade.recraft;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RecraftMenu extends Menu {

    public RecraftMenu(Player player) {
        super(player, "Recraft's", true, 3);
    }

    @Override
    public void handle() {
        clear();

        for (int slot = 0; slot < 9; slot++)
            addItem(slot, new Item(Material.RED_MUSHROOM, 64));

        for (int slot = 9; slot < 18; slot++)
            addItem(slot, new Item(Material.BROWN_MUSHROOM, 64));

        for (int slot = 18; slot < 27; slot++)
            addItem(slot, new Item(Material.BOWL, 64));

        display();
    }
}