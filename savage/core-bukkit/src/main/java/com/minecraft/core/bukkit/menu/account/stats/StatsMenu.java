package com.minecraft.core.bukkit.menu.account.stats;

import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.stats.BedWarsStatsMenu;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class StatsMenu extends Menu {

    private final Account target;

    public StatsMenu(Player player, Account target, Menu last) {
        super(player, "Estatísticas", last, 5);

        this.target = target;
    }

    @Override
    public void handle() {
        clear();

        // Apenas BedWars
        addItem(22, Item.of(Material.BED, "§a" + ServerType.BEDWARS.getName(), "§eClique para ver!")
                .flags(ItemFlag.values())
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new BedWarsStatsMenu(getPlayer(), this).handle();
                }));

        if (isReturnable())
            addBackButton();
        else
            addCloseButton();

        display();
    }
}
