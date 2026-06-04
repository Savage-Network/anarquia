package com.minecraft.core.bukkit.menu.server.party;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.api.party.Party;
import com.minecraft.core.api.party.type.PartyType;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class PartyMenu extends Menu {

    private final Account account;

    public PartyMenu(Player player) {
        super(player, "Parties Públicas", 5, 14);

        this.account = Core.getAccountController().of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        List<Party> list = Core.getPartyData().list(party -> party.isType(PartyType.PUBLIC));

        if (list.isEmpty())
            addErrorButton("§cNão há parties abertas...");
        else {

            buildPageItems(list, 10, (party, slot) -> {

                Account author = party.getAuthor();

                Item icon = Item.of(Material.SKULL_ITEM, 3, "§aParty de " + author.getNickname())
                        .skullByBase64(author.getSkin().getValue())
                        .lore("§7Membros: §f" + party.getMembersCount() + "/" + party.getSlots(),
                                "",
                                !party.isFull() ? "§eClique para entrar!" : "§cParty cheia.")
                        .click(event -> {

                            if (party.isFull()) {
                                sound(MenuSound.ERROR);

                                account.send("§cA party está cheia neste momento.");
                                return;
                            }

                            close();
                            sound(MenuSound.DONE);

                            party.add(account);

                            account.send("§aVocê entrou na party de " + author.getNickname() + ".");
                        });

                addItem(slot, icon);
            });
        }

        display();
    }
}
