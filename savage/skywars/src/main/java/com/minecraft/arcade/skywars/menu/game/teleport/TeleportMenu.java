package com.minecraft.arcade.skywars.menu.game.teleport;

import com.minecraft.arcade.skywars.arcade.arena.Arena;
import com.minecraft.arcade.skywars.user.User;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.bukkit.api.menu.Menu;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class TeleportMenu extends Menu {

    private final User user;

    public TeleportMenu(Player player) {
        super(player, "Teleportador", 1);

        this.user = (User) User.of(player.getUniqueId());
    }

    @Override
    public void handle() {
        clear();

        Arena arena = user.getArena();

        List<User> userList = arena.getUsers().stream()
                .map(u -> (User) u)
                .filter(u -> u.getContext().isAlive() && u.inState(ArcadeState.ALIVE))
                .collect(Collectors.toList());

        if (userList.isEmpty())
            addErrorButton(4, "§cNão há jogadores...");
        else {
            int slot = 0;
            for (User targetUser : userList) {
                Account account = targetUser.getAccount();

                Player targetPlayer = account.player();

                if (targetPlayer == null) continue;

                int lifePercent = Util.percentage((int) targetPlayer.getHealth(), (int) targetPlayer.getMaxHealth());

                String lifeColor = lifePercent >= 70 ? "§a"
                        : lifePercent <= 50 && lifePercent >= 20 ? "§e"
                        : "§c";

                final int finalSlot = slot;
                addItem(finalSlot, Item.of(Material.SKULL_ITEM, 3, account.getTag().getPrefix() + account.getNickname(),
                                "",
                                "§7Vida atual: " + lifeColor + lifePercent + "%",
                                "§7Kills: §a" + targetUser.getContext().getKills(),
                                "",
                                "§eClique para ir.")
                        .skullByBase64(account.getSkin().getValue())
                        .click(event -> {
                            close();
                            sound(MenuSound.DONE);

                            getPlayer().teleport(targetPlayer);
                        }));

                slot++;
            }
        }

        display();
    }
}

