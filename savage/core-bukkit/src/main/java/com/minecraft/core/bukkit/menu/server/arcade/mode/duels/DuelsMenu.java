package com.minecraft.core.bukkit.menu.server.arcade.mode.duels;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.account.stats.StatsInfoMenu;
import com.minecraft.core.bukkit.menu.server.arcade.editor.ArcadeEditorMenu;
import com.minecraft.core.bukkit.menu.server.arcade.mode.duels.collectible.CollectibleMenu;
import com.minecraft.core.bukkit.menu.server.arcade.mode.duels.preference.PreferenceMenu;
import com.minecraft.core.member.list.duels.DuelMember;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.DuelCollectible;
import com.minecraft.core.member.list.duels.metadata.objects.collectible.objects.type.DuelCollectibleType;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DuelsMenu extends Menu {

    private final Account account;
    private final DuelMember member;

    public DuelsMenu(Player player) {
        super(player, "Menu do Practice", 5);

        this.account = Core.getAccountController().of(player.getUniqueId());

        this.member = (DuelMember) account.loadMember(ServerType.DUELS);
    }

    @Override
    public void handle() {
        clear();

        if (member == null)
            addErrorButton("§cNão foi possível carregar os seus dados...");
        else {
            addItem(10, Item.of(Material.ANVIL, "§aEditar inventários",
                            "§7Altere os inventários",
                            "§7padrões dos modos.",
                            "",
                            "§eClique para ver!")
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        new ArcadeEditorMenu(getPlayer(), ServerType.DUELS, this).handle();
                    }));

            addItem(13, Item.of(Material.DIODE, "§aPreferências",
                            "§7Altere as preferências",
                            "§7nos modos.",
                            "",
                            "§eClique para ver!")
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        new PreferenceMenu(getPlayer(), this).handle();
                    }));

            handleCollectible(16, DuelCollectibleType.EFFECT_KILL);

            addItem(29, Item.of(Material.PAPER, "§aEstatísticas",
                            "§7Veja o seu desempenho",
                            "§7nas partidas.",
                            "",
                            "§eClique para ver!")
                    .click(event -> {
                        sound(MenuSound.PAGINATED);
                        new StatsInfoMenu(getPlayer(), account, ServerType.DUELS, this).handle();
                    }));
        }

        addCloseButton();

        display();
    }

    protected void handleCollectible(int slot, DuelCollectibleType type) {
        List<String> lore = new ArrayList<>();

        DuelCollectible chosen = member.getActiveCollectible(type);

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

        addItem(slot, type.getIcon().clone()
                .name("§a" + type.getName())
                .flags(ItemFlag.values())
                .lore(lore)
                .click(event -> {
                    sound(MenuSound.PAGINATED);
                    new CollectibleMenu(getPlayer(), member, type, this).handle();
                }));

    }
}
