package com.minecraft.core.bukkit.menu.server.arcade.custom.builder.map;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.payload.ArcadePayload;
import com.minecraft.core.arcade.room.map.Map;
import com.minecraft.core.bukkit.api.arcade.ArcadeConfig;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.stream.Collectors;

public class ArcadeMapSelectMenu extends Menu {

    private final ArcadeConfig config;

    public ArcadeMapSelectMenu(Player player, ArcadeConfig config, Menu last) {
        super(player, "Selecionando mapas...", last, 5, 21);

        this.config = config;
    }

    @Override
    public void handle() {
        clear();

        ArcadePayload payload = config.getPayload();

        if (payload == null)
            addErrorButton("§cNão foi possível carregar os dados do jogo...");
        else {
            List<Map> maps = payload.getMaps().stream()
                    .sorted((a, b) -> {
                        int selectedComparison = Boolean.compare(config.isSelected(b), config.isSelected(a));
                        if (selectedComparison != 0)
                            return selectedComparison;

                        return a.getName().compareToIgnoreCase(b.getName());
                    })
                    .collect(Collectors.toList());

            if (maps.isEmpty())
                addErrorButton("§cNão há mapas disponíveis...");
            else {

                buildPageItems(maps, 10, (map, slot) -> {
                    boolean selected = config.isSelected(map);

                    Item item = Item.of(Material.EMPTY_MAP, (selected ? "§a" : "§c") + map.getName())
                            .flags(ItemFlag.values());

                    if (selected)
                        item.enchantment(Enchantment.DURABILITY, 1);

                    item.lore("§eClique esquerdo: §fSelecionar",
                            "§eClique direito: §fRemover",
                            "",
                            selected ? "§cSelecionado." : "§bClique para selecionar!");

                    item.click(event -> {
                        if (event.isLeftClick()) {
                            if (selected) {
                                sound(MenuSound.ERROR);
                                return;
                            }

                            config.getMaps().add(map);
                        } else if (event.isRightClick()) {
                            if (!selected) {
                                sound(MenuSound.ERROR);
                                return;
                            }

                            config.getMaps().remove(map);
                        }

                        sound(MenuSound.DONE);
                        handle();
                    });

                    addItem(slot, item);
                });
            }
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
