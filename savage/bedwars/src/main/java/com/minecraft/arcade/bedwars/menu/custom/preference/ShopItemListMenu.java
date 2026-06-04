package com.minecraft.arcade.bedwars.menu.custom.preference;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.room.custom.RoomCustom;
import com.minecraft.core.arcade.room.custom.ShopConfig;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopItemListMenu extends Menu {

    private final Arena arena;
    private final BedShop shop;

    public ShopItemListMenu(Player player, Arena arena, BedShop shop, Menu last) {
        super(player, "Itens: " + shop.getName(), last, 6, 21);

        this.arena = arena;
        this.shop = shop;
    }

    @Override
    public void handle() {
        clear();

        RoomCustom custom = arena.getCustom();
        ShopConfig shopConfig = custom.getShopConfig();

        List<BedWarsItem> items = BedWarsItem.list(shop)
                .stream()
                .filter(item -> item.isAvailableForMode(arena.getType()))
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            addErrorButton(22, "§cNenhum item disponível.");
        } else {
            buildPageItems(items, 0, (item, slot) -> {
                ShopConfig.ItemConfig config = shopConfig.getItemConfig(item);
                boolean hasCustom = shopConfig.hasCustomConfig(item);

                List<String> lore = new ArrayList<>();
                lore.add("§7Moeda: " + config.getOre().getName());
                lore.add("§7Preço: §6" + config.getPrice());
                lore.add("§7Quantidade: §6" + config.getQuantity());
                lore.add("");
                
                if (hasCustom) {
                    lore.add("§aConfiguração customizada!");
                    lore.add("");
                }
                
                lore.add("§eClique para configurar!");
                lore.add("§eShift + Clique para resetar!");

                addItem(slot, Item.fromStack(item.getStack())
                        .name((hasCustom ? "§b" : "§a") + item.getName())
                        .lore(lore)
                        .flags(ItemFlag.values())
                        .click(event -> {
                            if (event.isShiftClick()) {
                                shopConfig.resetItemConfig(item);
                                sound(MenuSound.DONE);
                                getPlayer().sendMessage("§aItem resetado para o padrão!");
                                handle();
                            } else {
                                sound(MenuSound.PAGINATED);
                                new ShopItemEditMenu(getPlayer(), arena, item, this).handle();
                            }
                        }));
            });
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
