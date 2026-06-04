package com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.hotbar;

import com.minecraft.core.Core;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HotbarEditorMenu extends Menu {
    
    private final BedMember member;
    private final Menu previous;
    
    public HotbarEditorMenu(Player player, BedMember member, Menu previous) {
        super(player, "Editor de hotbar", 3);
        this.member = member;
        this.previous = previous;
    }
    
    @Override
    public void handle() {
        clear();
        
        // Linha 1: Slots da hotbar (0-8) - vidros vermelhos ou com categoria definida
        for (int i = 0; i < 9; i++) {
            final int slot = i; // Tornar final para uso na lambda
            BedShop assignedShop = member.getMetadata().getHotbarSlot(slot);
            
            if (assignedShop != null) {
                // Mostrar ícone da categoria se estiver definida
                addItem(slot, Item.fromStack(assignedShop.getIcon())
                        .name("§aSlot #" + (slot + 1) + ": " + assignedShop.getName())
                        .lore(
                                "",
                                "§7Categoria: §f" + assignedShop.getName(),
                                "",
                                "§eClique para remover!"
                        )
                        .click(event -> {
                            member.getMetadata().setHotbarSlot(slot, null);
                            member.saveMetadata(member.getMetadata());
                            
                            sound(MenuSound.SUCCESS);
                            getPlayer().sendMessage("§cCategoria removida do slot #" + (slot + 1) + "!");
                            
                            handle();
                        }));
            } else {
                // Vidro vermelho se vazio
                addItem(slot, Item.of(Material.STAINED_GLASS_PANE, 14)
                        .name("§cSlot #" + (slot + 1) + ": Vazio")
                        .lore(
                                "",
                                "§7Clique no item do meio",
                                "§7para adicionar categoria."
                        ));
            }
        }
        
        // Slot do meio (13): Botão para selecionar categoria
        addItem(13, Item.of(Material.EMERALD)
                .name("§aAdicionar categoria")
                .lore(
                        "",
                        "§7Clique para selecionar",
                        "§7uma categoria para adicionar",
                        "§7em um slot da hotbar."
                )
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new HotbarCategoryAssignMenu(getPlayer(), member, null, this).handle();
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
