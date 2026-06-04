package com.minecraft.core.bukkit.menu.server.arcade.custom;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.server.arcade.custom.builder.ArcadeBuilderMenu;
import com.minecraft.core.server.flag.ServerFlag;
import com.minecraft.core.server.type.ServerType;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.List;

@Setter
public class CustomArcadeMenu extends Menu {

    private ServerType server = ServerType.BUNGEE;

    public CustomArcadeMenu(Player player, Menu last) {
        super(player, "Selecionar modo", last, 4, 14);
    }

    @Override
    public void handle() {
        clear();

        if (server.equals(ServerType.BUNGEE)) {
            // Apenas BedWars e Duels
            List<ServerType> serverList = ServerType.list(server -> server.isArcade() && server.hasFlag(ServerFlag.CUSTOM_ROOM) 
                    && (server == ServerType.BEDWARS || server == ServerType.DUELS));

            if (serverList.isEmpty())
                addErrorButton("§cNão há servidores de jogos...");
            else {
                buildPageItems(serverList, 10, (server, slot) -> {
                    addItem(slot, Item.of(Material.getMaterial(server.getIconId()), "§a" + server.getName(),
                                    "§eClique para selecionar!")
                            .flags(ItemFlag.values())
                            .click(event -> {
                                setServer(server);
                                sound(MenuSound.PAGINATED);

                                setTitle("Selecionar modo: " + server.getName());

                                handle();
                            }));
                });
            }
        } else {
            List<ArcadeCategory> arcadeList = ArcadeCategory.of(server);

            if (arcadeList.isEmpty())
                addErrorButton("§cNão há jogos disponíveis...");
            else {

                int slot = 10, last = slot;
                for (ArcadeCategory arcade : arcadeList) {

                    for (Slot slotType : arcade.getSlots()) {
                        addItem(slot, Item.of(Material.getMaterial(arcade.getIconId()),
                                        "§a" + server.getName() + " " + arcade.getName(),
                                        "§eClique para configurar.")
                                .amount(slotType.ordinal())
                                .click(event -> {
                                    sound(MenuSound.PAGINATED);
                                    new ArcadeBuilderMenu(getPlayer(), arcade, slotType, this).handle();
                                }));

                        slot++;
                    }

                    if (slot == (last + 7)) {
                        slot += 2;
                        last = slot;
                    }
                }
            }
        }

        if (isReturnable())
            addItem(getTotalSlots() - 5, Item.of(Material.ARROW, "§aVoltar",
                            getLast() != null ? "§7Para " + (server.equals(ServerType.BUNGEE) ? getLast().getInitialTitle() : "Selecionar modo") : "")
                    .click(event -> {
                        if (getLast() == null) {
                            sound(MenuSound.ERROR);
                            return;
                        }

                        if (!server.equals(ServerType.BUNGEE)) {
                            setServer(ServerType.BUNGEE);
                            setTitle("Selecionar modo");

                            handle();
                        } else
                            getLast().handle();

                        sound(MenuSound.PAGINATED);
                    }), true);

        display();
    }
}
