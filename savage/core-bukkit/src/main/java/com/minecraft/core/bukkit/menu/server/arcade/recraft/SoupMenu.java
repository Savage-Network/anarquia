package com.minecraft.core.bukkit.menu.server.arcade.recraft;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SoupMenu extends Menu {

    public SoupMenu(Player player) {
        super(player, "Sopas", true, 4);
    }

    @Override
    public void handle() {
        clear();

        for (int slot = 0; slot < (getRows() * 9); slot++)
            addItem(slot, Item.of(Material.MUSHROOM_SOUP));

        display();
    }
}
