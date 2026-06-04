package com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.quick;

import com.minecraft.core.Constant;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.quick.favorite.FavoriteSelectMenu;
import com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.quick.share.ShareSearchMenu;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.share.ShareType;
import com.minecraft.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.core.member.list.bedwars.objects.menu.BedMenuMetadata;
import com.minecraft.core.member.list.bedwars.objects.menu.item.BedItem;
import com.minecraft.core.member.list.bedwars.objects.menu.type.BedMenuType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuickBuyMenu extends Menu {

    private final BedMember member;

    public QuickBuyMenu(Player player, BedMember member, Menu last) {
        super(player, "Editar " + BedShop.FAVORITE.getName().toLowerCase(), last, 6, 21);

        this.member = member;

        setAllowClickItemWithQuantity(true);
    }

    @Override
    public void handle() {
        clear();

        if (member == null)
            addErrorButton("§cNão foi possível recolher os seus dados...");
        else {

            BedMenuMetadata menu = member.getMenu(BedMenuType.FAVORITE);

            if (menu == null)
                addErrorButton("§cNão foi possível visualizar o seu inventário...");
            else {

                /* Adicionando Espaços Vázios */
                int emptySlot = 10, last = emptySlot;
                for (int i = 0; i < getMaxItems(); i++) {
                    int finalEmptySlot = emptySlot;

                    addItem(emptySlot, Item.of(Material.STAINED_GLASS_PANE, 14, "§cEspaço vázio!",
                                    "§7Este é um espaço para",
                                    "§7item favorito.",
                                    "",
                                    "§eClique esquerdo§7 para definir.")
                            .click(event -> {
                                if (event.isLeftClick()) {
                                    sound(MenuSound.PAGINATED);
                                    new FavoriteSelectMenu(getPlayer(), member, this, finalEmptySlot).handle();
                                } else
                                    sound(MenuSound.ERROR);
                            }));

                    emptySlot++;
                    if (emptySlot == (last + 7)) {
                        emptySlot += 2;
                        last = emptySlot;
                    }
                }

                List<BedItem> list = menu.getItemList();

                for (BedItem item : list) {
                    BedWarsItem root = BedWarsItem.of(item.getName());

                    if (root == null) continue;

                    List<String> lore = new ArrayList<>(root.getLore());

                    if (lore.isEmpty())
                        lore.add("§7Sem descrição.");

                    lore.addAll(Arrays.asList(
                            "",
                            "§eClique esquerdo§7 para substituir.",
                            "§eClique direito§7 para remover."
                    ));

                    addItem(item.getSlot() - 9, Item.fromStack(root.getStack())
                            .name("§a" + root.getName())
                            .flags(ItemFlag.values())
                            .lore(lore)
                            .click(event -> {
                                if (event.isRightClick()) {
                                    sound(MenuSound.DONE);

                                    member.removeFavoriteItem(root);

                                    handle();
                                    return;
                                }

                                if (event.isLeftClick()) {
                                    sound(MenuSound.PAGINATED);
                                    new FavoriteSelectMenu(getPlayer(), member, this, item.getSlot() - 9).handle();
                                }
                            }));
                }

                /* My Share */
                ShareType share = member.getMetadata().getShare();

                List<String> myShareLore = new ArrayList<>(share.getLore());

                myShareLore.addAll(Arrays.asList(
                        "",
                        "§7Opções disponíveis:"
                ));

                for (ShareType search : ShareType.values()) {
                    myShareLore.add(" " + search.getColor() + (share.equals(search) ? Constant.ARROW_ALT_SYMBOL + " " : "") + search.getName());
                }

                myShareLore.addAll(Arrays.asList(
                        "",
                        "§eClique para alternar!"));

                int id = share.equals(ShareType.MEDIUM) ? 4 : share.equals(ShareType.HARD) ? 1 : share.equals(ShareType.MAX) ? 14 : 7;

                addItem(getTotalSlots() - 9, Item.of(Material.WOOL, id, "§aMeu compartilhamento", myShareLore)
                        .click(event -> {
                            sound(MenuSound.DONE);
                            member.setShare(share.next());

                            handle();
                        }));

                addItem(getTotalSlots() - 1, Item.of(Material.ANVIL, "§aCompartilhar inventário",
                                "§7Compartilhe o seu estilo de",
                                "§7inventário com outros jogadores.",
                                "",
                                "§eClique para ver!")
                        .click(event -> {
                            sound(MenuSound.PAGINATED);
                            new ShareSearchMenu(getPlayer(), list, this).handle();
                        }));
            }
        }

        if (isReturnable())
            addBackButton();

        display();
    }
}
