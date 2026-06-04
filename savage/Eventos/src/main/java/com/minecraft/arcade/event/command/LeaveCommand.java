package com.minecraft.arcade.event.command;

import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.arcade.event.user.User;

public class LeaveCommand implements CommandInheritor {

    @Command(
            name = "sair",
            aliases = {"leave"}
    )
    public void leave(BukkitCommandContext context) {
        User user = (User) User.of(context.getPlayer().getUniqueId());

        user.getAccount().redirectToHub();
    }
}
