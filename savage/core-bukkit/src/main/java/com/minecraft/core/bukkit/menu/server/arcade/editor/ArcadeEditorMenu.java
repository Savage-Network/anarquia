package com.minecraft.core.bukkit.menu.server.arcade.editor;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.feature.ArcadeFeature;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class ArcadeEditorMenu extends Menu {

    private final ServerType server;

    public ArcadeEditorMenu(Player player, ServerType server, Menu last) {
        super(player, "O que deseja editar?", last, 4);

        this.server = server;
    }

    @Override
    public void handle() {
        clear();

        List<ArcadeCategory> list = ArcadeCategory.of(server);

        if (list.isEmpty())
            addErrorButton("§cNão há inventários para editar...");
        else {
            int slot = 10;
            for (ArcadeCategory arcade : list) {
                if (!arcade.hasFeature(ArcadeFeature.EDITABLE_MENU)) continue;

                addItem(slot, Item.of(Material.getMaterial(arcade.getIconId()), "§a" + arcade.getName(), "§eClique para editar!")
                        .click(event -> {
                            sound(MenuSound.PAGINATED);
                            new ArcadeEditingMenu(getPlayer(), arcade, this).handle();
                        }));

                slot++;
            }
        }

        if (isReturnable())
            addBackButton();
        else
            addCloseButton();

        display();
    }
}
