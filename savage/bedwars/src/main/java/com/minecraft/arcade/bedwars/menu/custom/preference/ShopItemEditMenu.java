package com.minecraft.arcade.bedwars.menu.custom.preference;

import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.room.custom.RoomCustom;
import com.minecraft.core.arcade.room.custom.ShopConfig;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.objects.enums.BedOre;
import com.minecraft.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class ShopItemEditMenu extends Menu {

    private final Arena arena;
    private final BedWarsItem item;

    public ShopItemEditMenu(Player player, Arena arena, BedWarsItem item, Menu last) {
        super(player, "Editar: " + item.getName(), last, 3);

        this.arena = arena;
        this.item = item;
        
        setAllowClickItemWithQuantity(true);
    }

    @Override
    public void handle() {
        clear();

        RoomCustom custom = arena.getCustom();
        ShopConfig shopConfig = custom.getShopConfig();
        ShopConfig.ItemConfig config = shopConfig.getItemConfig(item);

        // Item preview
        addItem(4, Item.fromStack(item.getStack())
                .name("§e" + item.getName())
                .lore("§7Configurando este item.")
                .flags(ItemFlag.values()));

        // Moeda
        addItem(11, Item.of(config.getOre().getMaterial(), "§aMoeda")
                .lore("§7Defina qual moeda será",
                        "§7usada para comprar.",
                        "",
                        "§7Moeda atual: " + config.getOre().getName(),
                        "",
                        "§eClique para trocar!")
                .click(event -> {
                    BedOre[] ores = BedOre.values();
                    int currentIndex = config.getOre().ordinal();
                    int nextIndex = (currentIndex + 1) % ores.length;
                    
                    config.setOre(ores[nextIndex]);
                    shopConfig.setItemConfig(item, config.getOre(), config.getPrice(), config.getQuantity());
                    
                    sound(MenuSound.DONE);
                    handle();
                }));

        // Preço
        int price = config.getPrice();
        addItem(13, Item.of(Material.GOLD_INGOT, "§ePreço")
                .amount(Math.min(price, 64))
                .lore("§7Defina o preço do item.",
                        "",
                        "§7Preço atual: §6" + price,
                        "",
                        "§eClique esquerdo: §f+1",
                        "§eShift + Esquerdo: §f+10",
                        "§eClique direito: §f-1",
                        "§eShift + Direito: §f-10")
                .click(event -> {
                    int change = event.isShiftClick() ? 10 : 1;
                    
                    if (event.isLeftClick()) {
                        config.setPrice(Math.min(price + change, 999));
                    } else if (event.isRightClick()) {
                        config.setPrice(Math.max(price - change, 0));
                    }
                    
                    shopConfig.setItemConfig(item, config.getOre(), config.getPrice(), config.getQuantity());
                    sound(MenuSound.DONE);
                    handle();
                }));

        // Quantidade
        int quantity = config.getQuantity();
        addItem(15, Item.fromStack(item.getStack())
                .name("§aQuantidade")
                .amount(Math.min(quantity, 64))
                .lore("§7Defina a quantidade do item.",
                        "",
                        "§7Quantidade atual: §6" + quantity,
                        "",
                        "§eClique esquerdo: §f+1",
                        "§eShift + Esquerdo: §f+10",
                        "§eClique direito: §f-1",
                        "§eShift + Direito: §f-10")
                .flags(ItemFlag.values())
                .click(event -> {
                    int change = event.isShiftClick() ? 10 : 1;
                    
                    if (event.isLeftClick()) {
                        config.setQuantity(Math.min(quantity + change, 64));
                    } else if (event.isRightClick()) {
                        config.setQuantity(Math.max(quantity - change, 1));
                    }
                    
                    shopConfig.setItemConfig(item, config.getOre(), config.getPrice(), config.getQuantity());
                    sound(MenuSound.DONE);
                    handle();
                }));

        if (isReturnable())
            addBackButton();

        display();
    }
}
