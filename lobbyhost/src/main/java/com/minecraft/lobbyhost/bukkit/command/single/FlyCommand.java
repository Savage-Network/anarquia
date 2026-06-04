package com.minecraft.lobbyhost.bukkit.command.single;

import com.minecraft.lobbyhost.bukkit.command.annotation.Command;
import com.minecraft.lobbyhost.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.lobbyhost.bukkit.command.structure.CommandInheritor;
import org.bukkit.entity.Player;

public final class FlyCommand implements CommandInheritor {

    @Command(name = "fly", aliases = {"voar"}, playerOnly = true,
            permission = "lobbyhost.fly",
            description = "Alterna modo voo")
    public void execute(BukkitCommandContext ctx) {
        Player player = ctx.player();
        boolean enable = !player.getAllowFlight();
        player.setAllowFlight(enable);
        player.setFlying(enable);
        ctx.reply(enable ? "§aFly ativado." : "§eFly desativado.");
    }
}
