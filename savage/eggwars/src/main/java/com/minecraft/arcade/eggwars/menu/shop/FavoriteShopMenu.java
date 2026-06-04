package com.minecraft.arcade.eggwars.menu.shop;

import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.arcade.eggwars.user.User;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.eggwars.EggMember;
import com.minecraft.core.member.list.eggwars.objects.enums.EggShop;
import com.minecraft.core.member.list.eggwars.objects.enums.item.EggWarsItem;
import com.minecraft.core.member.list.eggwars.objects.menu.item.EggItem;
import com.minecraft.core.member.list.eggwars.objects.menu.type.EggMenuType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavoriteShopMenu extends Menu {

    private final Arena arena;

    private final EggMember member;
    private final EggWarsItem item;

    public FavoriteShopMenu(Player player, EggWarsItem item, Menu last) {
        super(player, "Selecionando espaço...", last, 6, 21);

        User user = (User) User.of(player.getUniqueId());

        this.arena = user.getArena();
        this.member = user.getMember();

        this.item = item;
    }

    @Override
    public void handle() {
        clear();

        List<EggItem> list = member.getMenu(EggMenuType.FAVORITE).getItemList();

        /* Adicionando item selecionado */
        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.addAll(item.getLore());
        lore.addAll(Arrays.asList("", "§eAdicionando..."));

        addItem(4, Item.fromStack(item.getStack())
                .name("§c" + item.getName())
                .lore(lore));

        /* Adicionando Espaços Vázios */
        int slot = 19, last = slot;
        for (int i = 0; i < getMaxItems(); i++) {
            int finalSlot = slot;
            EggItem item = list.stream().filter(root -> root.getSlot() == finalSlot).findFirst().orElse(null);

            if (item != null) {
                EggWarsItem root = EggWarsItem.of(item.getName());

                if (root != null && !root.isAvailableForMode(arena.getType())) {
                    addItem(slot, buildIncompatibleItem());
                } else
                    addItem(slot, buildEmptySpace());
            } else
                addItem(slot, buildEmptySpace());

            slot++;
            if (slot == (last + 7)) {
                slot += 2;
                last = slot;
            }
        }

        // Adicionando os itens favoritos do jogador
        for (EggItem root : list) {
            EggWarsItem item = EggWarsItem.of(root.getName());

            if (item == null) continue;

            if (!item.isAvailableForMode(arena.getType()))
                addItem(root.getSlot(), buildIncompatibleItem());
            else
                addItem(root.getSlot(), Item.fromStack(item.getStack())
                        .name("§a" + item.getName())
                        .lore("§7Você não pode mexer."));
        }

        if (isReturnable())
            addBackButton(45);

        display();
    }

    protected Item buildEmptySpace() {
        return Item.of(Material.STAINED_GLASS_PANE, 14, "§cEspaço vázio!",
                        "§eClique para selecionar!")
                .click(event -> {
                    member.setFavoriteItem(event.getSlot(), item);

                    sound(MenuSound.DONE);

                    new ShopMenu(getPlayer(), EggShop.FAVORITE).handle();
                });
    }

    protected Item buildIncompatibleItem() {
        return Item.of(Material.STAINED_GLASS_PANE, 14, "§cItem incompatível!",
                "§7Este espaço está ocupado",
                "§7por um item incompatível.");
    }
}
