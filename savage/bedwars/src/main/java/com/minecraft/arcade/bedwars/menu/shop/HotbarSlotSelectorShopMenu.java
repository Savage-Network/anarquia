package com.minecraft.arcade.bedwars.menu.shop;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HotbarSlotSelectorShopMenu extends Menu {
    
    private final BedMember member;
    private final int hotbarSlot;
    private final Menu previous;
    
    public HotbarSlotSelectorShopMenu(Player player, BedMember member, int hotbarSlot, Menu previous) {
        super(player, "Adicionado no slot #" + (hotbarSlot + 1) + "...", 3);
        this.member = member;
        this.hotbarSlot = hotbarSlot;
        this.previous = previous;
    }
    
    @Override
    public void handle() {
        clear();
        
        // Mostrar categorias disponíveis
        int slot = 10;
        for (BedShop shop : BedShop.values()) {
            if (shop == BedShop.FAVORITE) continue; // Pular favoritos
            
            boolean isAssigned = member.getMetadata().getHotbarSlot(hotbarSlot) == shop;
            
            addItem(slot++, Item.fromStack(shop.getIcon())
                    .name((isAssigned ? "§a✔ " : "§7") + shop.getName())
                    .lore(
                            "",
                            isAssigned ? "§aJá definido neste slot!" : "§7Clique para definir",
                            isAssigned ? "" : "§7neste slot."
                    )
                    .click(event -> {
                        member.getMetadata().setHotbarSlot(hotbarSlot, shop);
                        member.saveMetadata(member.getMetadata());
                        
                        sound(MenuSound.SUCCESS);
                        getPlayer().sendMessage("§aCategoria §f" + shop.getName() + " §adefinida no slot #" + (hotbarSlot + 1) + "!");
                        
                        previous.handle();
                    }));
        }
        
        // Botão para remover categoria
        addItem(16, Item.of(Material.BARRIER)
                .name("§cRemover categoria")
                .lore(
                        "",
                        "§7Clique para remover a",
                        "§7categoria deste slot."
                )
                .click(event -> {
                    member.getMetadata().setHotbarSlot(hotbarSlot, null);
                    member.saveMetadata(member.getMetadata());
                    
                    sound(MenuSound.SUCCESS);
                    getPlayer().sendMessage("§cCategoria removida do slot #" + (hotbarSlot + 1) + "!");
                    
                    previous.handle();
                }));
        
        // Botão de voltar
        addItem(22, Item.of(Material.ARROW)
                .name("§aVoltar")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    previous.handle();
                }));
        
        display();
    }
}
