package com.minecraft.arcade.event.menu;

import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.util.Util;
import com.minecraft.arcade.event.Event;
import com.minecraft.arcade.event.game.Game;
import com.minecraft.arcade.event.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayersAlivesMenu extends Menu {

    private final Game game;

    public PlayersAlivesMenu(Player player) {
        super(player, "Jogadores vivos", 3, 18);

        this.game = Event.getGame();
    }

    @Override
    public void handle() {
        clear();

        List<User> users = game.getUsers().stream().toList();

        if (users.isEmpty())
            addErrorButton("§cNão há jogadores vivos.");
        else
            buildPageItems(users, 10, (user, slot) -> {

                Account account = user.getAccount();

                Player target = account.player();
                if (target == null) return;

                addItem(slot, Item.of(Material.SKULL_ITEM, 3, account.getColoredName(),
                                "",
                                "§7Vida: §c" + Util.formatDouble(target.getHealth()) + "❤",
                                "",
                                "§eClique para ir até o jogador.")
                        .skullByBase64(account.getSkin().getValue())
                        .click(event -> {
                            close();
                            sound(MenuSound.DONE);

                            getPlayer().teleport(target);
                        }));
            });

        display();
    }
}
