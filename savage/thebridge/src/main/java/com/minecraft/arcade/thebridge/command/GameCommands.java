package com.minecraft.arcade.thebridge.command;

import com.minecraft.arcade.thebridge.arcade.Arcade;
import com.minecraft.arcade.thebridge.arcade.arena.Arena;
import com.minecraft.arcade.thebridge.user.User;
import com.minecraft.core.Constant;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class GameCommands implements CommandInheritor {

    @Command(name = "playagain")
    public void playAgain(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        Arcade arcade = user.getArcade();

        Arena current = user.getArena();

        ArcadeRouteContext route = ArcadeRouteContext.builder()
                .arcade(arcade.getCategory())
                .slot(current.getSlot())
                .join(user.getJoin())
                .maxPlayers(current.getMaxPlayers())
                .build();

        if (account.isPartyOwner())
            route.setLink(account.getParty().getMembersId());

        Arena arena = arcade.findBestArena(player.getUniqueId(), route);

        if (arena == null) {
            player.sendMessage(Constant.NO_ROOM_AVAILABLE_MESSAGE);
            return;
        }

        current.quit(player);

        arena.join(player);

        if (route.isLinked())
            account.getParty().redirect(route);
    }
}
