package com.minecraft.core.bukkit.menu.server.arcade.mode.bedwars.quick.share;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.member.list.bedwars.BedMember;
import com.minecraft.core.member.list.bedwars.objects.enums.item.BedWarsItem;
import com.minecraft.core.member.list.bedwars.objects.menu.BedMenuMetadata;
import com.minecraft.core.member.list.bedwars.objects.menu.item.BedItem;
import com.minecraft.core.member.list.bedwars.objects.menu.type.BedMenuType;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.share.FavoriteItemShare;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;

public class ShareMenu extends Menu {

    private BedMember member;

    private final FavoriteItemShare share;

    public ShareMenu(Player player, FavoriteItemShare share) {
        super(player, "Favoritos de " + share.getSenderName(), 6, 21);

        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account != null)
            this.member = (BedMember) account.loadMember(ServerType.BEDWARS);

        this.share = share;
    }

    @Override
    public void handle() {
        clear();

        if (member == null)
            addErrorButton("§cNão foi possível recolher os seus dados...");
        else {

            /* Adicionando Espaços Vázios */
            int emptySlot = 10, last = emptySlot;
            for (int i = 0; i < getMaxItems(); i++) {
                addItem(emptySlot, Item.of(Material.STAINED_GLASS_PANE, 14, "§cEspaço vázio!",
                        "§7Este é um espaço para",
                        "§7item favorito."));

                emptySlot++;
                if (emptySlot == (last + 7)) {
                    emptySlot += 2;
                    last = emptySlot;
                }
            }

            List<BedItem> list = share.getItems();

            for (BedItem item : list) {
                BedWarsItem root = BedWarsItem.of(item.getName());

                if (root == null) continue;

                List<String> lore = new ArrayList<>(root.getLore());

                if (lore.isEmpty())
                    lore.add("§7Sem descrição.");

                addItem(item.getSlot(), Item.fromStack(root.getStack())
                        .name("§a" + root.getName())
                        .flags(ItemFlag.values())
                        .lore(lore));
            }

            BedMenuMetadata menu = member.getMenu(BedMenuType.FAVORITE);

            addItem(getTotalSlots() - 1, Item.of(Material.WOOL, 5, "§aSalvar favoritos",
                            "§7Clique para copiar os",
                            "§7favoritos de " + share.getSenderName() + ".",
                            "",
                            "§c§lAVISO: §cSubstituirá os",
                            "§cfavoritos atuais.")
                    .click(event -> {
                        close();
                        sound(MenuSound.DONE);

                        menu.getItemList().clear();
                        menu.getItemList().addAll(list);

                        member.updateMenu(menu);

                        getPlayer().sendMessage("§aOs favoritos de " + share.getSenderName() + "§a foram copiados.");

                        FavoriteItemShare.remove(share);
                    }));
        }

        addCloseButton();

        display();
    }
}
