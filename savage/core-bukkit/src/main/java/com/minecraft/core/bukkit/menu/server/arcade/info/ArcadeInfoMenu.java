package com.minecraft.core.bukkit.menu.server.arcade.info;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.manager.list.ArcadeManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ArcadeInfoMenu extends Menu {

    private final ArcadeManager manager;

    public ArcadeInfoMenu(Player player) {
        super(player, "Informações > Sistema Arcade", 5);

        this.manager = BukkitCore.getManager().getArcade();
    }

    @Override
    public void handle() {
        clear();

        addItem(4, Item.of(Material.BOOK, "§aDados gerais",
                        "",
                        "§7Jogos atuais: §f" + manager.getArcades().size(),
                        "§7Salas abertas: §f" + manager.getArenas().size())
                .updater(view -> {
                    if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                    Item item = getContents().get(4);

                    if (item != null) {
                        item.lore("",
                                "§7Jogos atuais: §f" + manager.getArcades().size(),
                                "§7Salas abertas: §f" + manager.getArenas().size(),
                                "");

                        view.setItem(4, item);

                        getPlayer().updateInventory();
                    }
                }));

        Set<ArcadeHolder> arcadeList = new HashSet<>(manager.getArcades())
                .stream().sorted(Comparator.comparingInt(arcade -> arcade.getCategory().ordinal())).collect(Collectors.toCollection(LinkedHashSet::new));

        int slot = 10, last = slot;
        for (ArcadeHolder arcade : arcadeList) {
            Set<Room> roomList = arcade.getRooms();

            int occupiedArenas = (int) roomList.stream().filter(room -> !room.isAvailable()).count(),
                    availableArenas = (roomList.size() - occupiedArenas);

            ArcadeCategory category = arcade.getCategory();

            final int finalSlot = slot;
            addItem(finalSlot, Item.of(Material.getMaterial(category.getIconId()), "§e" + category.getName(),
                            "",
                            "§7Total de salas: §f" + roomList.size(),
                            "§8* §7Abertas: §a" + availableArenas,
                            "§8* §7Ocupadas: §c" + occupiedArenas)
                    .amount(category.getSlots().get(0).ordinal())
                    .updater(view -> {
                        if (!view.getTitle().equalsIgnoreCase(getTitle())) return;

                        Item item = getContents().get(finalSlot);

                        if (item != null) {
                            Set<Room> rooms = arcade.getRooms();

                            int occupied = (int) rooms.stream().filter(room -> !room.isAvailable()).count(),
                                    available = (rooms.size() - occupied);

                            item.lore(
                                    "",
                                    "§7Total de salas: §f" + rooms.size(),
                                    "§8* §7Abertas: §a" + available,
                                    "§8* §7Ocupadas: §c" + occupied);

                            view.setItem(finalSlot, item);

                            getPlayer().updateInventory();
                        }
                    }));

            slot++;
            if (slot == (last + 7)) {
                slot += 2;
                last = slot;
            }
        }

        addCloseButton();

        display();
    }
}
