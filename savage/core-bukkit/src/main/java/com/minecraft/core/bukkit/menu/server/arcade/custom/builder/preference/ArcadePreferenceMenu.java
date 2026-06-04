package com.minecraft.core.bukkit.menu.server.arcade.custom.builder.preference;

import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.bukkit.api.arcade.ArcadeConfig;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ArcadePreferenceMenu extends Menu {

    private final ArcadeConfig config;

    public ArcadePreferenceMenu(Player player, ArcadeConfig config, Menu last) {
        super(player, "Configurações: " + config.getArcade().getServer().getName() + " " + config.getArcade().getName(),
                last, 3);

        this.config = config;

        setAllowClickItemWithQuantity(true);
    }

    @Override
    public void handle() {
        clear();

        Slot slot = config.getSlot();

        int minPlayers = slot.getMaxPlayers() / 2;

        int teamSize = (int) config.getProperty("team_size", minPlayers);

        addItem(10, Item.of(Material.SKULL_ITEM, 3, "§aTamanho do time")
                .skullByBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWE2YTIyZDA3NWVjNTQzYmNjOWEyYzZlMWE1ZWI5YWIwMDc0ZjNmZDhiYWJiYTU0NWUyZThhNjAzYTRjOGNhOCJ9fX0=")
                .amount(teamSize)
                .lore("§7Defina a quantidade de",
                        "§7jogadores por time.",
                        "",
                        "§7Quantidade: §a" + teamSize,
                        "",
                        "§eClique esquerdo: §fAumentar",
                        "§eClique direito: §fDiminuir")
                .click(event -> {
                    if (event.isLeftClick()) {
                        config.setProperty("team_size", teamSize + 1);

                        sound(MenuSound.DONE);
                        handle();
                    } else if (event.isRightClick()) {

                        if (teamSize <= minPlayers) {
                            sound(MenuSound.ERROR);
                            return;
                        }

                        config.setProperty("team_size", teamSize - 1);

                        sound(MenuSound.DONE);
                        handle();
                    }
                }));

        if (isReturnable())
            addBackButton();

        display();
    }
}
