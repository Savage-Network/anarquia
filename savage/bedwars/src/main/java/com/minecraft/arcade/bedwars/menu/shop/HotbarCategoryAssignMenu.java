package com.minecraft.arcade.bedwars.menu.shop;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HotbarCategoryAssignMenu extends Menu {
    
    private final BedMember member;
    private final BedShop selectedShop;
    private final Menu previous;
    
    public HotbarCategoryAssignMenu(Player player, BedMember member, BedShop selectedShop, Menu previous) {
        super(player, "Selecione o slot...", 3);
        this.member = member;
        this.selectedShop = selectedShop;
        this.previous = previous;
    }
    
    @Override
    public void handle() {
        clear();
        
        // Se selectedShop é null, mostrar lista de categorias para escolher
        if (selectedShop == null) {
            // Mostrar todas as categorias disponíveis
            int categorySlot = 10;
            for (BedShop shop : BedShop.values()) {
                if (shop == BedShop.FAVORITE) continue; // Pular favoritos
                
                addItem(categorySlot++, Item.fromStack(shop.getIcon())
                        .name("§a" + shop.getName())
                        .lore(
                                "",
                                "§7Clique para selecionar",
                                "§7esta categoria."
                        )
                        .click(event -> {
                            sound(MenuSound.PAGINATED);
                            new HotbarCategoryAssignMenu(getPlayer(), member, shop, previous).handle();
                        }));
            }
            
            // Botão de voltar
            addItem(22, Item.of(Material.ARROW)
                    .name("§aVoltar")
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        previous.handle();
                    }));
            
            display();
            return;
        }
        
        // Se selectedShop não é null, mostrar slots para escolher
        // Linha 1: Slots da hotbar (0-8) clicáveis
        for (int slot = 0; slot < 9; slot++) {
            addHotbarSlot(slot);
        }
        
        // Linha 2: Mostrar a categoria selecionada no centro
        addItem(13, Item.fromStack(selectedShop.getIcon())
                .name("§a" + selectedShop.getName())
                .lore(
                        "",
                        "§7Clique em um slot acima",
                        "§7para definir esta categoria."
                ));
        
        // Linha 3: Botão de voltar
        addItem(22, Item.of(Material.ARROW)
                .name("§aVoltar")
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    previous.handle();
                }));
        
        display();
    }
    
    private void addHotbarSlot(int slot) {
        BedShop assignedShop = member.getMetadata().getHotbarSlot(slot);
        
        boolean isOccupied = assignedShop != null;
        
        addItem(slot, Item.of(Material.STAINED_GLASS_PANE, isOccupied ? 1 : 5) // Laranja se ocupado, verde se livre
                .name(isOccupied ? "§6Slot #" + (slot + 1) + " §7(Ocupado)" : "§aSlot #" + (slot + 1))
                .lore(
                        "",
                        isOccupied ? "§7Atual: §f" + assignedShop.getName() : "§7Vazio",
                        "",
                        "§eClique para definir!"
                )
                .click(event -> {
                    member.getMetadata().setHotbarSlot(slot, selectedShop);
                    member.saveMetadata(member.getMetadata());
                    
                    sound(MenuSound.SUCCESS);
                    getPlayer().sendMessage("§aCategoria §f" + selectedShop.getName() + " §adefinida no slot #" + (slot + 1) + "!");
                    
                    previous.handle();
                }));
    }
}
