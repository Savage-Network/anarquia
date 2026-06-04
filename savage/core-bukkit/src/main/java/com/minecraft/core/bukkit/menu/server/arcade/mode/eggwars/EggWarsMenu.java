package com.minecraft.core.bukkit.menu.server.arcade.mode.eggwars;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.collectible.CollectibleMenu;
import com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.preference.PreferenceMenu;
import com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.quick.QuickBuyMenu;
import com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.stats.BedWarsStatsMenu;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EggWarsMenu extends Menu {

    private final BedMember member;

    public EggWarsMenu(Player player) {
        super(player, "Menu do EggWars", 6);

        Account account = Core.getAccountController().of(player.getUniqueId());

        this.member = (BedMember) account.loadMember(ServerType.EGGWARS);
    }

    @Override
    public void handle() {
        clear();

        /* Se o membro for nulo, não carregar os dados */
        if (member == null)
            addErrorButton("§cNão foi possível carregar os seus dados...");
        else {

            addItem(10, Item.of(Material.BREWING_STAND_ITEM, "§aEditar " + BedShop.FAVORITE.getName().toLowerCase(),
                            "§7Altere o inventário",
                            "§7dos seus itens " + BedShop.FAVORITE.getName().toLowerCase() + ".",
                            "",
                            "§eClique para ver!")
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        new QuickBuyMenu(getPlayer(), member, this).handle();
                    }));

            addItem(13, Item.of(Material.DIODE, "§aPreferências",
                            "§7Altere as preferências",
                            "§7nos modos.",
                            "",
                            "§eClique para ver!")
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        new PreferenceMenu(getPlayer(), member, this).handle();
                    }));

            addItem(16, Item.of(Material.PAPER, "§aEstatísticas",
                            "§7Veja o seu desempenho",
                            "§7nas partidas.",
                            "",
                            "§eClique para ver!")
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        new BedWarsStatsMenu(getPlayer(), this).handle();
                    }));

            // Carregar os colecionáveis
            handleCollectibles();
        }

        addCloseButton();

        display();
    }

    protected void handleCollectibles() {
        int initialSlot = 29;

        for (BedCollectibleType type : BedCollectibleType.values()) {
            List<String> lore = new ArrayList<>();

            BedCollectible chosen = member.getActiveCollectible(type);

            int collectibleCount = member.getCollectibleCount(type), itemCount = type.getItemCount();

            lore.addAll(Arrays.asList(
                    "§8" + collectibleCount + "/" + itemCount + " (" + Util.percentage(collectibleCount, itemCount) + "%)",
                    ""));

            lore.addAll(type.getLore());

            lore.addAll(Arrays.asList(
                    "",
                    "§7Escolhido: §a" + (chosen != null ? chosen.getName() : "Nenhuma"),
                    "",
                    "§eClique para ver!"));

            addItem(initialSlot, type.getIcon().clone()
                    .name("§a" + type.getName())
                    .flags(ItemFlag.values())
                    .lore(lore)
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        new CollectibleMenu(getPlayer(), member, type, this).handle();
                    }));

            initialSlot += 2;
        }
    }
}
