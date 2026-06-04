package com.minecraft.core.bukkit.menu.server.arcade.viewer;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.api.viewer.MenuViewer;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.core.util.list.serialization.Serialization;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ViewerMenu extends Menu {

    private final MenuViewer menu;

    public ViewerMenu(Player player, MenuViewer menu) {
        super(player, menu.getNickName(), 5);

        this.menu = menu;
    }

    @Override
    public void handle() {
        clear();

        addItem(0, Item.of(Material.PAPER, "§e" + menu.getNickName(),
                "§7Modo: §f" + menu.getMode(),
                "§7Horário: §f" + DateUtil.getDate(menu.getEndedAt()),
                "",
                "§7Vida: §4❤ §c" + Util.formatDouble(menu.getFinalLife() / 2)
        ));

        String[] split = menu.getBase64().split(" @ ");

        String internalData = split[0], hotbarData = split[1];

        List<ItemStack> hotbar = Serialization.itemStackArrayFromBase64(hotbarData);

        int slot = 9;
        for (ItemStack content : hotbar) {
            if (slot > 17) return;

            if (content == null)
                content = new ItemStack(Material.AIR);

            addItem(slot, new Item(content.getType(), content.getAmount(), content.getDurability()));
            slot++;
        }

        List<ItemStack> internal = Serialization.itemStackArrayFromBase64(internalData);

        slot = 18;
        for (ItemStack content : internal) {
            if (slot > 44) return;

            if (content == null) {
                content = new ItemStack(Material.AIR);
            }

            addItem(slot, new Item(content.getType(), content.getAmount(), content.getDurability()));
            slot++;
        }

        display();
    }
}
