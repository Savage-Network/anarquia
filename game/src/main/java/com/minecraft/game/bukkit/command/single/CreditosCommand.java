package com.minecraft.game.bukkit.command.single;

import com.minecraft.game.bukkit.command.annotation.Command;
import com.minecraft.game.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.game.bukkit.command.structure.CommandInheritor;
import com.minecraft.game.bukkit.menu.credits.CreditosMenus;

public final class CreditosCommand implements CommandInheritor {

    @Command(name = "creditos", aliases = {"credits", "credito", "devs"},
            playerOnly = true,
            description = "Abre o menu de creditos do servidor",
            usage = "/creditos")
    public void execute(BukkitCommandContext ctx) {
        CreditosMenus.open(ctx.player());
    }
}
