package com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.bar;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BarMenu extends Menu {

    private final List<BedShop> shops;

    public BarMenu(Player player, Menu last) {
        super(player, "Editar barra de itens", last, 6);

        this.shops = new ArrayList<>(Arrays.asList(BedShop.values()));
    }

    @Override
    public void handle() {
        clear();

        int slot = 10;
        for (BedShop shop : shops) {
            if (shop.equals(BedShop.FAVORITE)) continue;

            addItem(slot, Item.fromStack(shop.getIcon())
                    .name("§a" + shop.getName())
                    .flags(ItemFlag.values())
                    .lore("§7Insira o item no espaço",
                            "§7abaixo para definir sua posição.",
                            "",
                            "§eClique para mover!")
                    .click(event -> {

                    }));

            slot++;
        }

        for (int i = 18; i < 27; i++)
            addItem(i, Item.of(Material.STAINED_GLASS_PANE, 7, "§8⬆ §7Categorias", "§8⬇ §7Barra de itens"));

        addItem(getTotalSlots() - 4, Item.of(Material.BARRIER, "§cRedefinir barra",
                "§7Clique para voltar a",
                "§7barra de itens padrão."));

        if (isReturnable())
            addBackButton();

        display();
    }
}
