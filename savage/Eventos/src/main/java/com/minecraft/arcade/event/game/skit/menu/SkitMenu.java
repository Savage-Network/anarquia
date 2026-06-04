package com.minecraft.arcade.event.game.skit.menu;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.arcade.event.controller.SkitController;
import com.minecraft.arcade.event.game.skit.Skit;
import com.minecraft.arcade.event.game.skit.menu.info.SkitInfoMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

public class SkitMenu extends Menu {

    public SkitMenu(Player player) {
        super(player, "Kits disponíveis", 4, 14);
    }

    @Override
    public void handle() {
        clear();

        List<Skit> skits = SkitController.find();

        if (skits.isEmpty()) {
            addErrorButton("§cNenhum kit disponível.");
        } else {
            buildPageItems(skits.stream().sorted(Comparator.comparing(Skit::getName)).toList(), 10, (skit, slot) ->
                    addItem(slot, Item.of(Material.PAPER, "§a" + skit.getName(),
                                    "§7Autor: §e" + skit.getAuthorName(),
                                    "",
                                    "§eClique para ver detalhes.")
                            .click(event -> new SkitInfoMenu(getPlayer(), skit, this).handle())));
        }

        addBackButton();
        display();
    }
}
