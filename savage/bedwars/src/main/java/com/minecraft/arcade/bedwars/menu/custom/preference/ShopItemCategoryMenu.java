package com.minecraft.arcade.bedwars.menu.custom.preference;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import org.bukkit.entity.Player;

public class ShopItemCategoryMenu extends Menu {

    private final Arena arena;

    public ShopItemCategoryMenu(Player player, Arena arena, Menu last) {
        super(player, "Categorias da Loja", last, 3);

        this.arena = arena;
    }

    @Override
    public void handle() {
        clear();

        int slot = 10;
        for (BedShop shop : BedShop.values()) {
            if (shop == BedShop.FAVORITE) continue; // Pular favoritos

            addItem(slot, Item.fromStack(shop.getIcon())
                    .name("§a" + shop.getName())
                    .lore("§7Configure os itens da",
                            "§7categoria " + shop.getName() + ".",
                            "",
                            "§eClique para abrir!")
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        new ShopItemListMenu(getPlayer(), arena, shop, this).handle();
                    }));

            slot++;
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
